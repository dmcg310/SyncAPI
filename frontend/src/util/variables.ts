import type {EnvironmentVariable} from '@/types';

export function resolveVariables(
    text: string,
    variables: EnvironmentVariable[]
): { resolved: string; foundVariables: Array<{ key: string; found: boolean }> } {
    const variableMap = new Map(variables.map(v => [v.key, v.value]));
    const foundVariables: Array<{ key: string; found: boolean }> = [];

    const resolved = text.replace(/\{\{([^}]+)\}\}/g, (match, key) => {
        const trimmedKey = key.trim();
        const value = variableMap.get(trimmedKey);

        foundVariables.push({
            key: trimmedKey,
            found: value !== undefined
        });

        return value !== undefined ? value : match;
    });

    return {resolved, foundVariables};
}

export function containsVariables(text: string): boolean {
    return /\{\{[^}]+\}\}/.test(text);
}
