import { requireNativeModule } from 'expo-modules-core';

interface SnippetMetadata {
  title: string;
  category: string;
  description: string;
}

// Access the native JavaLens module
const JavaLens = requireNativeModule('JavaLens');

export async function magicOcrFix(rawCode: string): Promise<string> {
  return await JavaLens.magicOcrFix(rawCode);
}

export async function generateMetadata(code: string): Promise<SnippetMetadata> {
  return await JavaLens.generateMetadata(code);
}

export async function askProjectChat(context: string, question: string): Promise<string> {
  return await JavaLens.askProjectChat(context, question);
}

export function stitchCode(existing: string, newFrame: string): string {
  return JavaLens.stitchCode(existing, newFrame);
}

export default JavaLens;
