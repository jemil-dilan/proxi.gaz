// Every name listed in an object schema's `required` must be declared in its `properties`.
// Schemas composed with allOf/anyOf/oneOf are skipped: their properties may come from parts.
export default function requiredPropertiesDefined(schema) {
  if (schema === null || typeof schema !== "object" || !Array.isArray(schema.required)) {
    return [];
  }
  if (schema.allOf || schema.anyOf || schema.oneOf) {
    return [];
  }
  const properties = schema.properties ?? {};
  return schema.required
    .filter((name) => !Object.prototype.hasOwnProperty.call(properties, name))
    .map((name) => ({
      message: `Required property "${name}" is not defined in "properties".`,
      path: ["required", schema.required.indexOf(name)],
    }));
}
