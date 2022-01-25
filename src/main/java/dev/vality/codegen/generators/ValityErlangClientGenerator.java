package dev.vality.codegen.generators;

import io.swagger.codegen.*;

import io.swagger.models.Swagger;
import io.swagger.util.Json;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vality.codegen.utils.erlang.*;

public class ValityErlangClientGenerator extends DefaultCodegen implements CodegenConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(ValityErlangClientGenerator.class);

  // source folder where to write the files
  protected String sourceFolder = "src";
  protected String packageName = "swagger";
  protected String apiPath = "src";
  protected String apiVersion = "1.0.0";

  public ValityErlangClientGenerator() {
    super();

    // set the output folder here
    outputFolder = "generated-code/vality-erlang-client";

    /**
      * Models.  You can write model files using the modelTemplateFiles map.
      * if you want to create one template for file, you can do so here.
      * for multiple files for model, just put another entry in the `modelTemplateFiles` with
      * a different extension
      */
    modelTemplateFiles.clear();

    /**
      * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
      * as with models, add multiple entries with different extensions for multiple files per
      * class
      */
    apiTemplateFiles.put(
        "api.mustache",   // the template to use
        ".erl");       // the extension for each file to write

    /**
      * Template Location.  This is the location which templates will be read from.  The generator
      * will use the resource stream to attempt to read the templates.
      */
    embeddedTemplateDir = templateDir = "vality-erlang-client";

    /**
      * Reserved words.  Override this with reserved words specific to your language
      */
    setReservedWordsLowerCase(
        Arrays.asList(
            "after","and","andalso","band","begin","bnot","bor","bsl","bsr","bxor","case",
            "catch","cond","div","end","fun","if","let","not","of","or","orelse","receive",
            "rem","try","when","xor"
        )
    );

    instantiationTypes.clear();

    typeMapping.clear();
    typeMapping.put("enum",         "binary");
    typeMapping.put("date",         "binary");
    typeMapping.put("DateTime",     "binary");
    typeMapping.put("string",       "binary");
    typeMapping.put("char",         "binary");
    typeMapping.put("binary",       "binary");
    typeMapping.put("UUID",         "binary");
    typeMapping.put("password",     "binary");
    typeMapping.put("boolean",      "boolean");
    typeMapping.put("integer",      "integer");
    typeMapping.put("long",         "integer");
    typeMapping.put("float",        "float");
    typeMapping.put("double",       "float");
    typeMapping.put("number",       "float");
    typeMapping.put("array",        "list");
    typeMapping.put("List",         "list");
    typeMapping.put("map",          "map");
    typeMapping.put("object",       "object");
    typeMapping.put("file",         "file");
    typeMapping.put("ByteArray",    "byte");

    cliOptions.clear();
    cliOptions.add(new CliOption(CodegenConstants.PACKAGE_NAME, "Erlang package name (convention: lowercase).")
        .defaultValue(this.packageName));
    /**
      * Additional Properties.  These values can be passed to the templates and
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
        * Supporting Files.  You can write single files for the generator with the
        * entire object tree available.  If the input file has a suffix of `.mustache
        * it will be processed by the template engine.  Otherwise, it will be copied
        */
      supportingFiles.add(new SupportingFile("rebar.config.mustache", "", "rebar.config"));
      supportingFiles.add(new SupportingFile("app.src.mustache", "", "src" + File.separator + this.packageName + ".app.src"));
      supportingFiles.add(new SupportingFile("procession.mustache", "", toSourceFilePath("procession", "erl")));
      supportingFiles.add(new SupportingFile("utils.mustache", "", toSourceFilePath("utils", "erl")));
      supportingFiles.add(new SupportingFile("types.mustache", "", toPackageNameSrcFile("erl")));
      supportingFiles.add(new SupportingFile("validation.mustache", "", toSourceFilePath("validation", "erl")));
      supportingFiles.add(new SupportingFile("common_validator.mustache", "", toSourceFilePath("common_validator", "erl")));
      supportingFiles.add(new SupportingFile("param_validator.mustache", "", toSourceFilePath("param_validator", "erl")));
      supportingFiles.add(new SupportingFile("schema_validator.mustache", "", toSourceFilePath("schema_validator", "erl")));
      supportingFiles.add(new SupportingFile("schema.mustache", "", toSourceFilePath("schema", "erl")));
      writeOptional(outputFolder, new SupportingFile("README.mustache", "", "README.md"));
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
          return pattern.replaceAll("^/","").replaceAll("/$","");
      }
      return pattern;
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
  public CodegenType getTag() {
      return CodegenType.CLIENT;
  }

  /**
    * Configures a friendly name for the generator.  This will be used by the generator
    * to select the library with the -l flag.
    *
    * @return the friendly name for the generator
    */
  public String getName() {
      return "vality-erlang-client";
  }

  /**
    * Returns human-friendly help for the generator.  Provide the consumer with help
    * tips, parameters here
    *
    * @return A string value for the help message
    */
  public String getHelp() {
      return "erlang client help";
  }

  @Override
  public String toApiName(String name) {
      if (name.length() == 0) {
          return this.packageName + "_default_api";
      }
      return this.packageName + "_" + underscore(name) + "_api";
  }

  /**
    * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
    * those terms here.  This logic is only called if a variable matches the reseved words
    *
    * @return the escaped term
    */
  @Override
  public String escapeReservedWord(String name) {
      return "_" + name;  // add an underscore to the name
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

  /**
    * Location to write api files.  You can use the apiPackage() as defined when the class is
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
          LOGGER.warn(operationId + " (reserved word) cannot be used as method name. Renamed to " + underscore(sanitizeName("call_" + operationId)));
          operationId = "call_" + operationId;
      }

      return underscore(operationId);
  }

  @Override
  public String toApiFilename(String name) {
      return toModuleName(name) + "_api";
  }

  @Override
  public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
      Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
      List<CodegenOperation> operationList = (List<CodegenOperation>) operations.get("operation");
      for (CodegenOperation op : operationList) {
          op.httpMethod = op.httpMethod.toLowerCase();
          if (op.path != null) {
              op.path = op.path.replaceAll("\\{(.*?)\\}", ":$1");
          }
      }
      return objs;
  }

  @Override
  public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
    Swagger swagger = (Swagger) objs.get("swagger");
    if (swagger != null) {
        try {
            objs.put("swagger-json", Json.mapper().writer(new ErlangJsonPrinter()).with(new ErlangJsonFactory()).writeValueAsString(swagger));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    return super.postProcessSupportingFileData(objs);
  }

  public String getDefaultTemplateDir() {
    return templateDir;
  }

  public void setPackageName(String packageName) {
      this.packageName = packageName;
  }

  protected String toModuleName(String name) {
      return this.packageName + "_" + underscore(name.replaceAll("-", "_"));
  }

  protected String toSourceFilePath(String name, String extension) {
      return "src" + File.separator +  toModuleName(name) + "." + extension;
  }

  protected String toPackageNameSrcFile(String extension) {
      return "src" + File.separator + this.packageName + "." + extension;
  }
}
