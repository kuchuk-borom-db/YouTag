module.exports = {
  parser: '@typescript-eslint/parser',
  parserOptions: {
    project: 'tsconfig.json',
    tsconfigRootDir: __dirname,
    sourceType: 'module',
  },
  plugins: ['@typescript-eslint/eslint-plugin'],
  extends: [
    'plugin:@typescript-eslint/recommended',
  ],
  root: true,
  env: {
    node: true,
    jest: true,
  },
  ignorePatterns: ['.eslintrc.js'],
  rules: {
    '@typescript-eslint/interface-name-prefix': 'off',
    '@typescript-eslint/explicit-function-return-type': 'off',
    '@typescript-eslint/explicit-module-boundary-types': 'off',
    '@typescript-eslint/no-explicit-any': 'off',
    'no-restricted-imports': 'off',
    '@typescript-eslint/no-namespace': 'off',
    '@typescript-eslint/no-unused-vars': 'off',
    '@typescript-eslint/no-restricted-imports': [
      'error',
      {
        patterns: [
          // Allow imports from 'api' folder inside any module.ts file
          {
            group: ['!**/api/**'],
            message: "Internal module 'api' folder is allowed for imports",
          },
          // Restrict imports from any internal folder inside *.module.ts files
          {
            group: ['**/internal/**'],
            message: "Internal module stuff can't be imported directly",
          },
        ],
      },
    ],
  },
};
