package org.milyn.javabean.performance.generator;

import freemarker.template.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class GenerateXML {
	public static void main(String[] args) throws IOException, TemplateException {
		Locale.setDefault(Locale.US);

		boolean simple = false;

		Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);
		// Specify the data source where the template files come from.
		// Here I set a file directory for it:
		cfg.setDirectoryForTemplateLoading(new File("src/test/resources/templates"));

		// Specify how templates will see the data-model. This is an advanced topic...
		// but just use this:
		cfg.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_21).build());

		Template temp;
		String name;
		if(simple) {
			temp = cfg.getTemplate("simple.ftl");
			name = "simple";
		} else {
			temp = cfg.getTemplate("extended.ftl");
			name = "orders";
		}

		String path = "";

		write(temp, 1, path + name +"-1.xml", simple);
		write(temp, 50, path + name +"-50.xml", simple);
		write(temp, 500, path + name +"-500.xml", simple);
		write(temp, 5000, path + name +"-5000.xml", simple);
		write(temp, 50000, path + name +"-50000.xml", simple);
		write(temp, 500000, path + name +"-500000.xml", simple);

        System.out.println("done");

	}

	private static void write(Template template, int numCustomers, String fileName, boolean simple) throws TemplateException,
			IOException {
		System.out.println("Writing " + fileName);

		Map<String, TemplateSequenceModel> root = new HashMap<String, TemplateSequenceModel>();
		if(simple) {
			root.put("customers", new SimpleGenerator(numCustomers));
		} else {
			root.put("customers", new CustomerGenerator(numCustomers));
		}

		Writer out = null;
		try {
			out = createWriter(fileName);

			template.process(root, out);
		} finally {
			closeWriter(out);
		}
	}

	private static Writer createWriter(final String filepath) {

		try {
			File file = new File(filepath);

			file.mkdirs();
			if(file.exists()) {
				file.delete();
			}
			file.createNewFile();

			return new FileWriter(file);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void closeWriter(Writer writer) {
		try {
			if(writer != null) {
				writer.close();
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
