package io.swagger.codegen.languages;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;

import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenType;
import io.swagger.models.Model;
import io.swagger.models.Operation;

public class JavaFis3ServerCodegen extends AbstractJavaJAXRSServerCodegen {
	private static final String APPLICATION_SERVICE_ENDPOINT = "Endpoint";

	@Override
	public CodegenType getTag() {
		return CodegenType.SERVER;
	}

	@Override
	public String getName() {
		return "java-fis3";
	}

	@Override
	public String getHelp() {
		return "FIS3";
	}

	public JavaFis3ServerCodegen() {
		super();

		templateDir = embeddedTemplateDir = "JavaFis3";
		library = "JavaFis3";

		// remove some CLI options
		removeCliOption(cliOptions, "java8");
		removeCliOption(cliOptions, "dateLibrary");
		removeCliOption(cliOptions, USE_BEANVALIDATION);

		// no documentation for FIS3
		modelDocTemplateFiles.remove("model_doc.mustache");
		apiDocTemplateFiles.remove("api_doc.mustache");
		apiTestTemplateFiles.remove("api_test.mustache");

		// Default generation will create interface
		// we will provide optional default implementation which will not be created if the corresponding file already exists.
		// apiTemplateFiles.put("apiImplementation.mustache", ".java");

		// Default values for FIS3
		dateLibrary = "java8";
		java8Mode = true;
		supportJava6 = false;
		useBeanValidation = true;
		typeMapping.put("date", "ZonedDateTime");
		outputFolder = "output"; // CLI:
		withXml = true;

		// Things FIS3 does not use
		additionalProperties.remove("jackson");

		// Custom properties...
		String project = "basis";
		// artifactId = artifactId; // CLI: artifactId
		apiPackage = "com.hlag.fis." + project + ".core.adapter.ws.xxx"; // CLI: apiPackage
		modelPackage = "com.hlag.fis." + project + ".core.adapter.ws.xxx.model"; // CLI: modelPackage
		testPackage = apiPackage;
		//title = "Generated Server"; // CLI: title 

		// fixed properties for code generation
		sourceFolder = paths("src", "gen", "java");

		// TODO change bean generation to XML style according to FIS3 guideline
	}

	@Override
	public boolean shouldOverwrite(String filename) {
		return true; // TODO for testing
		//		Path path = Paths.get(filename);
		//		if ("model".equals(path.getParent().getFileName().toString()))
		//			return !Files.exists(path);
		//		else
		//			// TODO filter implementation files so custom code will not be overwritten
		//			return super.shouldOverwrite(filename);
	}

	@Override
	public String apiFileFolder() {
		return paths(outputFolder, sourceFolder, apiPackage().replace('.', File.separatorChar));
	}

	@Override
	public String toApiName(final String name) {
		String computed = name;
		if (computed.length() == 0) {
			computed = "Default";
		}
		computed = sanitizeName(computed);
		return camelize(computed) + APPLICATION_SERVICE_ENDPOINT;
	}

	/**
	 * @see io.swagger.codegen.DefaultCodegen#addOperationToGroup(java.lang.String,
	 *      java.lang.String, io.swagger.models.Operation, io.swagger.codegen.CodegenOperation,
	 *      java.util.Map)
	 * @see io.swagger.codegen.languages.JavaJerseyServerCodegen#addOperationToGroup(java.lang.String,
	 *      java.lang.String, io.swagger.models.Operation, io.swagger.codegen.CodegenOperation,
	 *      java.util.Map)
	 */
	@Override
	public void addOperationToGroup(
			String tag,
			String resourcePath,
			Operation operation,
			CodegenOperation co,
			Map<String, List<CodegenOperation>> operations) {
		String basePath = resourcePath;
		if (basePath.startsWith("/")) {
			basePath = basePath.substring(1);
		}
		int pos = basePath.indexOf("/");
		if (pos > 0) {
			basePath = basePath.substring(0, pos);
		}

		if (basePath == "") {
			basePath = "default";
		} else {
			if (co.path.startsWith("/" + basePath)) {
				co.path = co.path.substring(("/" + basePath).length());
			}
			co.subresourceOperation = !co.path.isEmpty();
		}
		List<CodegenOperation> opList = operations.get(basePath);
		if (opList == null) {
			opList = new ArrayList<CodegenOperation>();
			operations.put(basePath, opList);
		}
		opList.add(co);
		co.baseName = basePath;
	}

	@Override
	public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
		// remove notes if they are empty
		@SuppressWarnings("unchecked")
		Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
		if (operations != null) {
			@SuppressWarnings("unchecked")
			List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
			for (CodegenOperation operation : ops) {
				if (Strings.isNullOrEmpty(operation.notes)) {
					operation.notes = null;
				}
			}
		}

		// do things from super class
		return super.postProcessOperations(objs);
	}

	@Override
	public CodegenModel fromModel(String name, Model model, Map<String, Model> allDefinitions) {
		CodegenModel cm = super.fromModel(name, model, allDefinitions);
//		cm.parent = "CompositeDomainObject";
//		cm.imports.add("com.hlag.fis.buildingblock.core.domain.CompositeDomainObject");
		return cm;
	}

	private static String paths(String first, String... more) {
		StringBuilder sb = new StringBuilder();
		sb.append(first);
		for (String next : more) {
			sb.append(File.separator).append(next);
		}
		return sb.toString();
	}

	/**
	 * Remove a CLI option from the list of available options
	 * 
	 * @param cliOptions the available CLI options
	 * @param optionToRemove the name of the CLI option to remove
	 */
	private static void removeCliOption(Iterable<CliOption> cliOptions, String optionToRemove) {
		Iterator<CliOption> it = cliOptions.iterator();
		while (it.hasNext()) {
			if (optionToRemove.equals(it.next().getOpt())) {
				it.remove();
			}
		}
	}
}
