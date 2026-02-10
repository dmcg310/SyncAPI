import React, {useState} from 'react';
import CopyButton from './CopyButton';

interface CodeSnippetProps {
    code: string;
    language?: string;
}

const CodeSnippet: React.FC<CodeSnippetProps> = ({code, language = 'cURL'}) => {
    const [isExpanded, setIsExpanded] = useState(false);

    const lineLimit = 10;
    const lines = code.split('\n');
    const shouldTruncate = lines.length > lineLimit;
    const displayCode = shouldTruncate && !isExpanded
        ? lines.slice(0, lineLimit).join('\n') + '\n...'
        : code;

    return (
        <div className="relative group">
            <div className="border border-neutral-200 rounded-lg overflow-x-auto">
                <div className="bg-neutral-100 px-4 py-2 border-b border-neutral-200">
                    <div className="flex items-center justify-between">
                        <span className="text-md font-semibold text-neutral-700">{language}</span>
                        <CopyButton text={code} label="Copy"/>
                    </div>
                </div>
                <pre className="bg-neutral-50 p-4 text-md font-mono text-neutral-800 whitespace-pre-wrap">
                    {displayCode}
                </pre>
            </div>

            {shouldTruncate && (
                <button
                    onClick={() => setIsExpanded(!isExpanded)}
                    className="mt-2 text-md text-primary-600 hover:text-primary-700 font-medium cursor-pointer"
                >
                    {isExpanded ? 'Show less' : 'Show more'}
                </button>
            )}
        </div>
    );
};

export default CodeSnippet;
