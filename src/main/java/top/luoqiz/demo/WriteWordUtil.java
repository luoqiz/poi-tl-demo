package top.luoqiz.demo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.HyperLinkTextRenderData;
import com.deepoove.poi.data.MiniTableRenderData;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.TextRenderData;

/**
 * @author Admin
 *
 */
public class WriteWordUtil {

	public void writeDocx(String path, Map<String, String> map) throws Exception {
		InputStream is = new FileInputStream(path);
		XWPFDocument doc = new XWPFDocument(is);
		// XWPFWordExtractor extractor = new XWPFWordExtractor(doc) ;
		// String text = extractor.getText();
		// System.out.println(text);
		// CoreProperties coreProps = extractor.getCoreProperties();
		// this.printCoreProperties(coreProps);
		// this.close(is);
	}

	/**
	 * 替换段落里面的变量
	 * 
	 * @param doc    要替换的文档
	 * @param params 参数
	 */
	private void replaceInPara(XWPFDocument doc, Map<String, Object> params) {
		Iterator<XWPFParagraph> iterator = doc.getParagraphsIterator();
		XWPFParagraph para;
		while (iterator.hasNext()) {
			para = iterator.next();
			this.replaceInPara(para, params);
		}
	}

	/**
	 * 替换段落里面的变量
	 * 
	 * @param para   要替换的段落
	 * @param params 参数
	 */
	private void replaceInPara(XWPFParagraph para, Map<String, Object> params) {
		List<XWPFRun> runs;
		Matcher matcher;
		if (this.matcher(para.getParagraphText()).find()) {
			runs = para.getRuns();
			for (int i = 0; i < runs.size(); i++) {
				XWPFRun run = runs.get(i);
				String runText = run.toString();
				matcher = this.matcher(runText);
				if (matcher.find()) {
					while ((matcher = this.matcher(runText)).find()) {
						runText = matcher.replaceFirst(String.valueOf(params.get(matcher.group(1))));
					}
					// 直接调用XWPFRun的setText()方法设置文本时，在底层会重新创建一个XWPFRun，把文本附加在当前文本后面，
					// 所以我们不能直接设值，需要先删除当前run,然后再自己手动插入一个新的run。
					para.removeRun(i);
					if (runText.equals("null")) {
						runText = "";
					}
					para.insertNewRun(i).setText(runText);
				}
			}
		}
	}

	/**
	 * 替换表格里面的变量
	 * 
	 * @param doc    要替换的文档
	 * @param params 参数
	 */
	private void replaceInTable(XWPFDocument doc, Map<String, Object> params) {
		Iterator<XWPFTable> iterator = doc.getTablesIterator();
		XWPFTable table;
		List<XWPFTableRow> rows;
		List<XWPFTableCell> cells;
		List<XWPFParagraph> paras;
		while (iterator.hasNext()) {
			table = iterator.next();
			rows = table.getRows();
			for (XWPFTableRow row : rows) {
				cells = row.getTableCells();
				for (XWPFTableCell cell : cells) {

					String cellTextString = cell.getText();
					for (Entry<String, Object> e : params.entrySet()) {
						if (cellTextString.contains("${" + e.getKey() + "}"))
							cellTextString = cellTextString.replace("${" + e.getKey() + "}", e.getValue().toString());
					}
					cell.removeParagraph(0);
					if (cellTextString.contains("${") && cellTextString.contains("}")) {
						cellTextString = "";
					}
					cell.setText(cellTextString);
//                    paras = cell.getParagraphs();
//					for (XWPFParagraph para : paras) {
//						this.replaceInPara(para, params);
//					}

				}
			}
		}
	}

	/**
	 * 正则匹配字符串
	 * 
	 * @param str
	 * @return
	 */
	private Matcher matcher(String str) {
		Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(str);
		return matcher;
	}

	/**
	 * 关闭输入流
	 * 
	 * @param is
	 */
	private void close(InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 关闭输出流
	 * 
	 * @param os
	 */
	private void close(OutputStream os) {
		if (os != null) {
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * word占位用${object}有缺陷不能填充图片
	 * 
	 * @param filePath
	 * @param params
	 * @throws Exception
	 */
	public static String templateWrite(String filePath, Map<String, Object> params, String outFilePath)
			throws Exception {
		InputStream is = new FileInputStream(filePath);
		WriteWordUtil writeWordUtil = new WriteWordUtil();
		XWPFDocument doc = new XWPFDocument(is);
		// 替换段落里面的变量
		writeWordUtil.replaceInPara(doc, params);
		// 替换表格里面的变量
		writeWordUtil.replaceInTable(doc, params);
		OutputStream os = new FileOutputStream(outFilePath);
		doc.write(os);
		writeWordUtil.close(os);
		writeWordUtil.close(is);
		os.flush();
		os.close();
		return "";
	}

	/**
	 * word占位用{{object}}比较完美可以填充图片
	 * 
	 * @param filePath
	 * @param params
	 * @param outFilePath
	 * @return
	 * @throws Exception
	 */
	public static String templateWrite2(String filePath, Map<String, Object> params, String outFilePath)
			throws Exception {
		XWPFTemplate template = XWPFTemplate.compile(filePath).render(params);
		FileOutputStream out = new FileOutputStream(outFilePath);
		template.write(out);
		out.flush();
		out.close();
		template.close();
		return "";
	}

	public static String templateWrite3(String filePath, Entity entity, String outFilePath) throws Exception {
		XWPFTemplate template = XWPFTemplate.compile(filePath).render(entity);
		FileOutputStream out = new FileOutputStream(outFilePath);
		template.write(out);
		out.flush();
		out.close();
		template.close();
		return "";
	}

	public static void main(String[] args) throws Exception {
		//第一种
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("XMMC", "Poi-tl 模板引擎99999999999999999999999999999999999999999");
		param.put("link", new HyperLinkTextRenderData("website.", "http://www.deepoove.com"));
		templateWrite2("D:\\test\\1.docx",param,"D:\\test\\8.docx");

		//第二种
		Entity entity = new Entity();
		entity.setDesc("王章");
		entity.setUrl(new HyperLinkTextRenderData("website.", "http://www.deepoove.com"));
		
		//第三种
		RowRenderData header = RowRenderData.build(new TextRenderData("FFFF00", "姓名"),
				new TextRenderData("FFFF00", "学历"));

		RowRenderData row1 = RowRenderData.build("李四", "博士");
		RowRenderData row2 = RowRenderData.build(new HyperLinkTextRenderData("王五", "http://www.deepoove.com"),
				new HyperLinkTextRenderData("博士后", "http://www.deepoove.com"));

		entity.setTable(new MiniTableRenderData(header, Arrays.asList( row1, row2)));
		templateWrite3("D:\\test\\1.docx", entity, "D:\\test\\9.docx");
	}
}