const toString = Object.prototype.toString;
const isFinite = Number.isFinite;
const isInteger = Number.isInteger;
const isArray = Array.isArray;

module.exports = function(...options) {
  for (const option of options) {
    let {data, type} = option;
    let realType = typeof data;

    switch (type) {
      case "boolean":
        if (realType === "boolean") break;
        return false;
      case "number":
        if (realType === "number") break;
        return false;
      case "integer":
        if (isInteger(data)) break;
        return false;
      case "positive":
        if (realType === "number" && !isNaN(data) && isFinite(data) && data > 0)
          break;
        return false;
      case "positive-integer":
        if (isInteger(data) && !isNaN(data) && isFinite(data) && data > 0)
          break;
        return false;
      case "non-negative":
        if (
          realType === "number" &&
          !isNaN(data) &&
          isFinite(data) &&
          data >= 0
        )
          break;
        return false;
      case "non-negative-integer":
        if (isInteger(data) && !isNaN(data) && isFinite(data) && data >= 0)
          break;
        return false;
      case "string":
        if (realType === "string" && data.length) break;
        return false;
      case "array":
        if (isArray(data)) break;
        return false;
      case "positive-array":
        if (toString.call(data) === "[object Array]") {
          for (let i = 0; i < data.length; i++)
            if (
              typeof data[i] !== "number" ||
              isNaN(data[i]) ||
              !isFinite(data[i]) ||
              data[i] <= 0
            )
              return false;
          break;
        }
        return false;
      case "object":
        if (toString.call(data) === "[object Object]") break;
        return false;
    }
  }

  return true;
};
