# Swagger-codegen generator for Vality

## Overview
This is a repository containing a custom `swagger-codegen` generator used in valitydev projects.

Last tested `swagger-codegen` version: **v2.4.25**

## Building

```bash
$> mvn package
```

## Usage

Generate a client library:
```bash
$> java -cp vality-erlang-codegen-swagger-codegen-1.0.0.jar:swagger-codegen-cli.jar io.swagger.codegen.SwaggerCodegen generate -l vality-erlang-client -i swagger.yaml -o erlang-client
```

Generate a server stub:
```bash
$> java -cp vality-erlang-codegen-swagger-codegen-1.0.0.jar:swagger-codegen-cli.jar io.swagger.codegen.SwaggerCodegen generate -l vality-erlang-server -i swagger.yaml -o erlang-server
```

The following additional debug options are available for all codegen targets:
* `-DdebugSwagger` prints the OpenAPI Specification as interpreted by the codegen
* `-DdebugModels` prints models passed to the template engine
* `-DdebugOperations` prints operations passed to the template engine
* `-DdebugSupportingFiles` prints additional data passed to the template engine

## Why not `3.0.0`?

Support for the `3.0.0` branch of `swagger-codegen` is planned in the future. It requires braking changes to template code and some minor changes to generator classes.
