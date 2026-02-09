import React, {useState} from 'react';
import {MdCheck, MdContentCopy} from 'react-icons/md';

interface CopyButtonProps {
    text: string;
    label?: string;
    className?: string;
}

const CopyButton: React.FC<CopyButtonProps> = ({text, label = 'Copy', className = ''}) => {
    const [copied, setCopied] = useState(false);

    const handleCopy = async () => {
        try {
            await navigator.clipboard.writeText(text);
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        } catch (err) {
            console.error('Failed to copy text:', err);
        }
    };

    return (
        <button
            onClick={handleCopy}
            className={`flex items-center gap-2 px-3 py-1.5 text-sm font-medium text-neutral-700 bg-white border border-neutral-200 rounded-md hover:bg-neutral-50 transition-colors ${className} cursor-pointer`}
            title={copied ? 'Copied!' : 'Copy to clipboard'}
        >
            {copied ? (
                <>
                    <MdCheck size={16} className="text-success-600"/>
                    Copied!
                </>
            ) : (
                <>
                    <MdContentCopy size={16}/>
                    {label}
                </>
            )}
        </button>
    );
};

export default CopyButton;
