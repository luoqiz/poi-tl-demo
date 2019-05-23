package top.luoqiz.demo;

import com.deepoove.poi.data.HyperLinkTextRenderData;
import com.deepoove.poi.data.MiniTableRenderData;

import lombok.Data;

@Data
public class Entity {
	private HyperLinkTextRenderData url;
	private String desc;
	private MiniTableRenderData table;

}
