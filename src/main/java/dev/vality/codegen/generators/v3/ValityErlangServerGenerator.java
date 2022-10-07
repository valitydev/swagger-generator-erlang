package dev.vality.codegen.generators.v3;

import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vality.codegen.utils.erlang.*;

public class ValityErlangServerGenerator extends DefaultCodegenConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValityErlangServerGenerator.class);

    protected String sourceFolder = "src";
    protected String packageName = "openapi";
    protected String apiPath = "src";
    protected String apiVersion = "1.0.0";

    public ValityErlangServerGenerator() {
        super();

        // set the output folder here
        outputFolder = "generated-code/vality-erlang-server";

        /**
         * Models. You can write model files using the modelTemplateFiles map.
         * if you want to create one template for file, you can do so here.
         * for multiple files for model, just put another entry in the
         * `modelTemplateFiles` with
         * a different extension
         */
        modelTemplateFiles.clear();

        /**
         * Api classes. You can write classes for each Api file with the
         * apiTemplateFiles map.
         * as with models, add multiple entries with different extensions for multiple
         * files per
         * class
         */
        apiTemplateFiles.put(
                "handler.mustache", // the template to use
                ".erl"); // the extension for each file to write

        /**
         * Template Location. This is the location which templates will be read from.
         * The generator
         * will use the resource stream to attempt to read the templates.
         */
        templateDir = "vality-erlang-server";

        /**
         * Reserved words. Override this with reserved words specific to your language
         */
        setReservedWordsLowerCase(
                Arrays.asList(
                        "after", "and", "andalso", "band", "begin", "bnot", "bor", "bsl", "bsr", "bxor", "case",
                        "catch", "cond", "div", "end", "fun", "if", "let", "not", "of", "or", "orelse", "receive",
                        "rem", "try", "when", "xor"));

        instantiationTypes.clear();

        typeMapping.clear();

        typeMapping.put("enum", "binary");
        typeMapping.put("date", "binary");
        typeMapping.put("DateTime", "binary");
        typeMapping.put("string", "binary");
        typeMapping.put("char", "binary");
        typeMapping.put("binary", "binary");
        typeMapping.put("UUID", "binary");
        typeMapping.put("password", "binary");
        typeMapping.put("boolean", "boolean");
        typeMapping.put("integer", "integer");
        typeMapping.put("long", "integer");
        typeMapping.put("float", "float");
        typeMapping.put("double", "float");
        typeMapping.put("number", "float");
        typeMapping.put("array", "list");
        typeMapping.put("List", "list");
        typeMapping.put("map", "map");
        typeMapping.put("object", "object");
        typeMapping.put("file", "file");
        typeMapping.put("ByteArray", "byte");

        cliOptions.clear();
        cliOptions.add(new CliOption(CodegenConstants.PACKAGE_NAME, "Erlang package name (convention: lowercase).")
                .defaultValue(this.packageName));
        /**
         * Additional Properties. These values can be passed to the templates and
         * are available in models, apis, and supporting files
         */
        additionalProperties.put("apiVersion", apiVersion);
        additionalProperties.put("apiPath", apiPath);
    }

    @Override
    public void processOpts() {
        super.processOpts();
        if (additionalProperties.containsKey(CodegenConstants.PACKAGE_NAME)) {
            setPackageName((String) additionalProperties.get(CodegenConstants.PACKAGE_NAME));
        } else {
            additionalProperties.put(CodegenConstants.PACKAGE_NAME, packageName);
        }

        /**
         * Supporting Files. You can write single files for the generator with the
         * entire object tree available. If the input file has a suffix of `.mustache
         * it will be processed by the template engine. Otherwise, it will be copied
         */
        supportingFiles.add(new SupportingFile("rebar.config.mustache", "", "rebar.config"));
        supportingFiles.add(
                new SupportingFile("app.src.mustache", "", "src" + File.separator + this.packageName + ".app.src"));
        supportingFiles.add(new SupportingFile("router.mustache", "", toSourceFilePath("router", "erl")));
        supportingFiles.add(new SupportingFile("utils.mustache", "", toSourceFilePath("utils", "erl")));
        supportingFiles.add(new SupportingFile("types.mustache", "", toPackageNameSrcFile("erl")));
        supportingFiles.add(new SupportingFile("handler_api.mustache", "", toSourceFilePath("handler_api", "erl")));
        supportingFiles.add(new SupportingFile("logic_handler.mustache", "", toSourceFilePath("logic_handler", "erl")));
        supportingFiles.add(new SupportingFile("validation.mustache", "", toSourceFilePath("validation", "erl")));
        supportingFiles
                .add(new SupportingFile("common_validator.mustache", "", toSourceFilePath("common_validator", "erl")));
        supportingFiles
                .add(new SupportingFile("param_validator.mustache", "", toSourceFilePath("param_validator", "erl")));
        supportingFiles
                .add(new SupportingFile("schema_validator.mustache", "", toSourceFilePath("schema_validator", "erl")));
        supportingFiles
                .add(new SupportingFile("custom_validator.mustache", "", toSourceFilePath("custom_validator", "erl")));
        supportingFiles.add(new SupportingFile("schema.mustache", "", toSourceFilePath("schema", "erl")));
        writeOptional(outputFolder, new SupportingFile("README.mustache", "", "README.md"));
    }

    @Override
    public String apiPackage() {
        return apiPath;
    }

    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     * @see io.swagger.codegen.CodegenType
     */
    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    /**
     * Configures a friendly name for the generator. This will be used by the
     * generator
     * to select the library with the -l flag.
     *
     * @return the friendly name for the generator
     */
    @Override
    public String getName() {
        return "vality-erlang-server";
    }

    /**
     * Returns human-friendly help for the generator. Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    @Override
    public String getHelp() {
        return "Generates an Erlang server library (beta) using the Swagger Codegen project. By default, " +
                "it will also generate service classes, which can be disabled with the `-Dnoservice` environment variable.";
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return this.packageName + "_default_handler";
        }
        return this.packageName + "_" + underscore(name) + "_handler";
    }

    /**
     * Escapes a reserved word as defined in the `reservedWords` array. Handle
     * escaping
     * those terms here. This logic is only called if a variable matches the
     * reserved words
     *
     * @return the escaped term
     */
    @Override
    public String escapeReservedWord(String name) {
        if (this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;
    }

    /**
     * If the pattern contains "/" in the beginning or in the end
     * remove those "/" symbols.
     *
     * @param pattern the pattern (regular expression)
     * @return the pattern with delimiter
     */
    @Override
    public String addRegularExpressionDelimiter(String pattern) {
        if (pattern != null) {
            return pattern.replaceAll("^/", "").replaceAll("/$", "");
        }
        return pattern;
    }

    /**
     * Location to write api files. You can use the apiPackage() as defined when the
     * class is
     * instantiated
     */
    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String toModelName(String name) {
        return camelize(toModelFilename(name));
    }

    @Override
    public String toOperationId(String operationId) {
        // method name cannot use reserved keyword, e.g. return
        if (isReservedWord(operationId)) {
            LOGGER.warn(operationId + " (reserved word) cannot be used as method name. Renamed to "
                    + camelize(sanitizeName("call_" + operationId)));
            operationId = "call_" + operationId;
        }

        return camelize(operationId);
    }

    @Override
    public String toApiFilename(String name) {
        return toHandlerName(name);
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        List<CodegenOperation> operationList = (List<CodegenOperation>) operations.get("operation");
        for (CodegenOperation op : operationList) {
            if (op.path != null) {
                op.path = op.path.replaceAll("\\{(.*?)\\}", ":$1");
            }
        }
        return objs;
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        OpenAPI openAPI = (OpenAPI) objs.get("openAPI");
        if (openAPI != null) {
            try {
                objs.put("openapi-json", Json.mapper().writer(new ErlangJsonPrinter()).with(new ErlangJsonFactory())
                        .writeValueAsString(openAPI));
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return super.postProcessSupportingFileData(objs);
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    protected String toHandlerName(String name) {
        return toModuleName(name) + "_handler";
    }

    protected String toModuleName(String name) {
        return this.packageName + "_" + underscore(name.replaceAll("-", "_"));
    }

    protected String toSourceFilePath(String name, String extension) {
        return "src" + File.separator + toModuleName(name) + "." + extension;
    }

    protected String toPackageNameSrcFile(String extension) {
        return "src" + File.separator + this.packageName + "." + extension;
    }

    protected String toIncludeFilePath(String name, String extension) {
        return "include" + File.separator + toModuleName(name) + "." + extension;
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove ' to avoid code injection
        return input.replace("'", "");
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        // ref: http://stackoverflow.com/a/30421295/677735
        return input.replace("-ifdef", "- if def").replace("-endif", "- end if");
    }

    @Override
    protected String getTemplateDir() {
        return templateDir;
    }

    @Override
    public String getDefaultTemplateDir() {
        return templateDir;
    }

}
