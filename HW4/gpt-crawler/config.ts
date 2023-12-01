import { Config } from "./src/config";

export const defaultConfig: Config = {
  // url: "https://www.builder.io/c/docs/developers",
  // match: "https://www.builder.io/c/docs/**",
  url: "https://pytorch.org/docs/stable",
  match: "https://pytorch.org/docs/stable/**",
  maxPagesToCrawl: 50,
  outputFileName: "output.json",
};
