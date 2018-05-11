package io.swagger.codegen.languages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;

import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenType;
import io.swagger.models.Operation;

public class JavaFis3ServerCodegen extends AbstractJavaJAXRSServerCodegen/* AbstractJavaCodegen */ {
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

		// embeddedTemplateDir =
		templateDir = embeddedTemplateDir = "JavaFis3";
		library = "JavaFis3";

		// remove some CLI options
		removeCliOption(cliOptions, "title");
		removeCliOption(cliOptions, "java8");
		removeCliOption(cliOptions, "dateLibrary");
		removeCliOption(cliOptions, USE_BEANVALIDATION);

		// no documentation for FIS3
		modelDocTemplateFiles.remove("model_doc.mustache");
		apiDocTemplateFiles.remove("api_doc.mustache");

		// add interfaces
		//apiTemplateFiles.put("apiService.mustache", ".java");

		// Default values for FIS3
		dateLibrary = "java8";
		java8Mode = true;
		supportJava6 = false;
		useBeanValidation = true;
		typeMapping.put("date", "ZonedDateTime");
		outputFolder = "output"; // CLI:
		withXml = true;

		// Custom properties...
		// TODO make these properties customizable for Maven integration
		// artifactId = artifactId; // CLI: artifactId
		apiPackage = "com.hlag.fis.basis.core.example.api"; // CLI: apiPackage
		modelPackage = "com.hlag.fis.basis.core.example.model"; // CLI: modelPackage
		testPackage = "com.hlag.fis.basis.core.example.test";
		title = "Generated Server"; // CLI: title (oben aber entfernt)

		// TODO change bean generation to XML style according to FIS3 guideline
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
