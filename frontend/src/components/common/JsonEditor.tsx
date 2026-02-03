import React from 'react';
import Editor from '@monaco-editor/react';

interface JsonEditorProps {
    value: string;
    onChange: (value: string) => void;
    disabled?: boolean;
    height?: string;
    placeholder?: string;
}

const JsonEditor: React.FC<JsonEditorProps> = ({
                                                   value,
                                                   onChange,
                                                   disabled = false,
                                                   height = '200px'
                                               }) => {
    return (
        <div className={`border border-neutral-300 rounded-lg overflow-hidden ${disabled ? 'opacity-60' : ''}`}>
            <Editor
                height={height}
                defaultLanguage="json"
                value={value}
                onChange={(val) => onChange(val || '')}
                options={{
                    readOnly: disabled,
                    minimap: {enabled: false},
                    fontSize: 14,
                    fontFamily: "'JetBrains Mono', 'Fira Code', monospace",
                    lineNumbers: 'on',
                    scrollBeyondLastLine: false,
                    automaticLayout: true,
                    tabSize: 2,
                    wordWrap: 'on',
                    folding: true,
                    bracketPairColorization: {enabled: true},
                    formatOnPaste: true,
                    formatOnType: true,
                    scrollbar: {
                        vertical: 'auto',
                        horizontal: 'auto',
                        verticalScrollbarSize: 8,
                        horizontalScrollbarSize: 8
                    },
                    padding: {top: 8, bottom: 8},
                    renderLineHighlight: 'line',
                    contextmenu: true,
                    quickSuggestions: false
                }}
                theme="vs-light"
                loading={
                    <div className="flex items-center justify-center h-full text-neutral-400 text-sm">
                        Loading editor...
                    </div>
                }
            />
        </div>
    );
};

export default JsonEditor;
