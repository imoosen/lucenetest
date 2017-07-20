package com.imoosen.lucene.imoosen;




import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by [mengsen] on 2017/7/20 0020.
 *
 * @Description: [一句话描述该类的功能]
 * @UpdateUser: [mengsen] on 2017/7/20 0020.
 */
public class LuceneTest {

    private static String indexPath ="E://lucene//index";

    private static String dataPath = "E://lucene//data";

    private static IndexWriter writer;//这个类用来写入索引

    /**
     * 创建索引文件夹
     *
     */

    public static Directory setIndex(String index)throws Exception{

        Path file = Paths.get(index);
        //Directory 类：表示索引文件存储位置的抽象类。
        Directory dir  = FSDirectory.open(file);
        //Analyzer类：用来对文档内容进行分词处理，Analyzer 类是一个抽象类，它有多个实现，
        // 针对不同的语言和应用需要选择适合的Analyzer，Analyzer会把分词后的内容交给IndexWriter来建立索引。
        Analyzer analyzer =  new StandardAnalyzer();
        //
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        writer = new IndexWriter(dir,config);
        return dir;

    }

    /**
     * 将文件写入Document
     * @param file
     * @return
     * @throws Exception
     */

    public static Document queryDocument(File file)throws Exception{

        Document doc = new Document();

        doc.add(new Field("contents",new FileReader(file),TextField.TYPE_NOT_STORED));
        doc.add(new Field("fileName",file.getName(),TextField.TYPE_STORED));
        doc.add(new Field("fullpath",file.getCanonicalPath(), TextField.TYPE_STORED));
        return doc;

    }
    //关闭indexWriter
    public static void close() throws IOException {
        writer.close();
    }
    /**
     * 将文件写入索引
     * @param file
     * @throws Exception
     */
    public static void documentIndex(File file)throws Exception{
        Document doc = queryDocument(file);
        writer.addDocument(doc);
    }
    //下面这个类是FileFilter的实现类，用来过滤符合条件的文档。
    private static class TextFilesFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            return pathname.getName().toLowerCase().endsWith(".txt");
        }
    }

    /**
     * 获取txt结尾的文件
     * @param dataPath
     * @param filesFilter
     * @return
     * @throws Exception
     */
    public static int getFile(String dataPath,TextFilesFilter filesFilter)throws Exception{

        File[] files = new File(dataPath).listFiles();

        for (File file : files) {
            if(!file.isDirectory()&&!file.isHidden()&&file.exists()&&file.canRead()&&(filesFilter==null)||filesFilter.accept(file))
                documentIndex(file);
            else
                System.out.println("can not find files or other problems");
        }
       return writer.numDocs();

    }

    //这个方法是搜索索引的方法，传入索引路径和查询表达式
    public static void search(Directory dir,String query) throws IOException, ParseException {
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
        Query q = parser.parse(query);
        long start = System.currentTimeMillis();
        TopDocs hits = searcher.search(q, 10);
        long end = System.currentTimeMillis();
        System.out.println(">>>>>>"+hits.totalHits);
        System.out.println("how long it takes to search: " + (end - start));
        for (ScoreDoc scoredoc : hits.scoreDocs
                ) {
            Document doc = searcher.doc(scoredoc.doc);
            System.out.println(doc.get("fullpath"));
        }
    }


    public static void main(String[] args)throws Exception {

        Directory dir = setIndex(indexPath);

        int num = getFile(dataPath,new LuceneTest.TextFilesFilter());

        System.out.println("共几个文件："+num);

        search(dir,"sen");

        close();
    }
}
