package com.osm2xp.utils.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.io.Files;
import com.osm2xp.gui.Activator;

public class ScaleChangeHelper {
	
	private static final String SCALE_KEYWORD = "SCALE";

	public static void changeScale(File facadeFile, double factor) throws IOException {
		List<String> lines = Files.readLines(facadeFile, Charset.forName("utf-8"));
		lines = lines.stream().map(str -> processScale(str, factor)).collect(Collectors.toList());
		java.nio.file.Files.write(Paths.get(facadeFile.toURI()),lines,Charset.forName("utf-8"));
	}

	private static String processScale(String str, double factor) {
		int idx = str.indexOf(SCALE_KEYWORD);
		if (idx >= 0) {
			StringBuilder builder = new StringBuilder(str.substring(0, idx + SCALE_KEYWORD.length()));
			String valuesStr = str.substring(idx + SCALE_KEYWORD.length());
			String[] valuesArr = valuesStr.split(" ");
			for (String val : valuesArr) {
				if (!val.trim().isEmpty()) {
					builder.append(" ");
					builder.append(String.format("%1.9f", Double.parseDouble(val) * factor));
				}
			}
			return builder.toString();
		}
		return str;
	}

	public static String getScaleStr(File facadeFile) {
		if (facadeFile.isFile()) {
			try {
				List<String> lines = Files.readLines(facadeFile, Charset.forName("utf-8"));
				for (String string : lines) {
					string = string.trim();
					if (string.startsWith(SCALE_KEYWORD)) {
						return string.substring(SCALE_KEYWORD.length()).trim();
					}
				}
			} catch (IOException e) {
				Activator.log(e);
			}
		}
		return "";
	}
	
//	public static void testChangeScale() {
//		File folder = new File("d:\\util\\xplane\\tmpfacades\\");
//		if (folder.isDirectory()) {
//			File[] listFiles = folder.listFiles();
//			for (File file : listFiles) {
//				if (file.getName().endsWith(".fac")) {
//					try {
//						changeScale(file, 2.5);
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//	}
//	
//	public static void main(String[] args) {
//		testChangeScale();
//	}
}
