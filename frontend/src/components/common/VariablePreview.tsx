import React from 'react';
import type {EnvironmentVariable} from '@/types';
import {containsVariables, resolveVariables} from '../../util/variables';
import {IoWarning} from "react-icons/io5";

interface VariablePreviewProps {
    text: string;
    variables: EnvironmentVariable[];
    activeEnvironmentName?: string;
}

const VariablePreview: React.FC<VariablePreviewProps> = ({
                                                             text,
                                                             variables,
                                                             activeEnvironmentName
                                                         }) => {
    if (!containsVariables(text)) {
        return null;
    }

    const {resolved, foundVariables} = resolveVariables(text, variables);
    const unresolvedVars = foundVariables.filter(v => !v.found);
    const hasUnresolved = unresolvedVars.length > 0;

    return (
        <div className="mt-2 space-y-1">
            <div className="flex items-start gap-2 text-sm">
                <span className="text-neutral-500 font-medium shrink-0">Preview:</span>
                <span className="text-neutral-700 font-mono break-all">{resolved}</span>
            </div>

            {hasUnresolved && (
                <div className="flex items-start gap-2 text-sm text-amber-600">
                    <span className="shrink-0"><IoWarning size={20}/></span>
                    <span>
                        Unresolved variables: {unresolvedVars.map(v => `{{${v.key}}}`).join(', ')}
                        {activeEnvironmentName && ` in environment "${activeEnvironmentName}"`}
                    </span>
                </div>
            )}

            {variables.length === 0 && (
                <div className="flex items-start gap-2 text-sm text-amber-600">
                    <span className="shrink-0"><IoWarning size={20}/></span>
                    <span>
                        No active environment selected. Variables will not be resolved.
                    </span>
                </div>
            )}
        </div>
    );
};

export default VariablePreview;
