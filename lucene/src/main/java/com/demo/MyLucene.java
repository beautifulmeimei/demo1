package com.demo;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import com.hankcs.lucene.HanLPAnalyzer;

public class MyLucene {

	/**
	 * 写入索引库
	 * @throws Exception 
	 */
	@Test
	public void indexWrite() throws Exception {
		//打开索引库
		Directory arg0 = FSDirectory.open(Paths.get("H:\\index"));
		//指定分词器：标准分词器
		//IndexWriterConfig arg1 = new IndexWriterConfig( new StandardAnalyzer() );
		IndexWriterConfig arg1 = new IndexWriterConfig( new HanLPAnalyzer());
		//提供一个写入索引库的对象
		IndexWriter indexWriter = new IndexWriter(arg0, arg1);
		//得到数据源所在目录
		File file = new File("H:\\source");
		//得到数据源目录中所有文件
		File[] files = file.listFiles();
		//遍历
		for (File file2 : files) {
			
			//文件标题
			String name = file2.getName();
			//文件内容
			String readFileToString = FileUtils.readFileToString(file2);
			//文件的路径
			String path = file2.getPath();
			//文件的大小
			long sizeOf = FileUtils.sizeOf(file2);
			
			//参数一：域字段名。参数二：解析的具体值。参数三：是否存储。
			Field textField = new TextField("name", name, Store.YES);
			Field textField2 = new TextField("readFileToString", readFileToString, Store.YES);
			Field storedField = new StoredField("path",path);
			Field storedField2 = new LongPoint("sizeOf", sizeOf);
			
			//创建文档
			Document document = new Document();
			document.add(textField);
			document.add(textField2);
			document.add(storedField);
			document.add(storedField2);
			//把文档写入到索引库
			indexWriter.addDocument(document);
		}
		//关闭资源
		indexWriter.close();
	}
	@Test
	public void deleteAll() throws Exception {
		//打开索引库
		Directory arg0 = FSDirectory.open(Paths.get("H:\\index"));
		// 指定分词器：标准分词器
		IndexWriterConfig arg1 = new IndexWriterConfig();
		// 提供一个写入索引库的对象
		IndexWriter indexWriter = new IndexWriter(arg0, arg1);
		indexWriter.deleteAll();
		indexWriter.close();
	}
	
	/**
	 * 查询索引库
	 * @throws Exception 
	 */
	@Test
	public void queryIndex() throws Exception {
		//开打索引库目录
		Directory directory = FSDirectory.open(Paths.get("H:\\index"));
		//创建一个indexReader对象
		IndexReader indexReader = DirectoryReader.open(directory);
		//把indexReader对象进行二次封装
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		//查询所有数据的查询器
		//Query query1 = new MatchAllDocsQuery();
		//不分词单域查询
		//Query query2 = new TermQuery(new Term("name","中国人"));
		//组合查询
		//BooleanClause booleanClause1 = new BooleanClause(query1, Occur.MUST);
		//BooleanClause booleanClause2 = new BooleanClause(query2, Occur.MUST_NOT);
		//BooleanQuery query = new BooleanQuery.Builder().add(booleanClause1).add(booleanClause2).build();
		//范围查询,条件必须是数字
		//Query query = LongPoint.newRangeQuery("sizeOf", 10, 500);
		//分词单域查询
		//QueryParser parser = new QueryParser("name",new HanLPAnalyzer());
		//Query query = parser.parse("spring是框架");
		//分词多域查询
		MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[] {"name","readFileToString"}, new HanLPAnalyzer());
		Query query = parser.parse("spring是框架");
		//查询索引库indexSearcher对象中search方法只能查询到文档数量和文档编号的集合
		TopDocs docs = indexSearcher.search(query, 10);
		//获取文档总数量
		 int totalHits = docs.totalHits;
		 System.out.println("获取文档总数量"+totalHits);
		//获取文档编号的集合
		 ScoreDoc[] scoreDocs = docs.scoreDocs;
		 for (ScoreDoc scoreDoc : scoreDocs) {
			 //得到每个文档编号
			 int i = scoreDoc.doc;	
			 System.out.println("得到每个文档编号"+i);
			 //通过文档编号查询文档
			 Document doc = indexSearcher.doc(i);
			 System.out.println("通过文档编号查询文档fname:"+doc.get("name"));
		}
		 //关闭资源
		 indexReader.close();
		}
}
