import React from 'react';
import {MdFileDownload} from 'react-icons/md';
import type {OpenApiSpec} from '@/types';

interface ExportButtonProps {
    spec: OpenApiSpec;
}

const ExportButton: React.FC<ExportButtonProps> = ({spec}) => {
    const sanitizeFilename = (name: string): string => {
        return name
            .toLowerCase()
            .replace(/[^a-z0-9]+/g, '-')
            .replace(/^-+|-+$/g, '');
    };

    const handleExport = () => {
        const baseFilename = sanitizeFilename(spec.info.title);
        const filename = `${baseFilename}-openapi.json`;

        const content = JSON.stringify(spec, null, 2);
        const mimeType = 'application/json';
        const blob = new Blob([content], {type: mimeType});

        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);

        link.click();

        document.body.removeChild(link);

        URL.revokeObjectURL(url);
    };

    return (
        <button
            onClick={handleExport}
            className="flex items-center gap-2 px-3 py-2 text-md font-medium text-neutral-700 bg-white border border-neutral-200 rounded-lg hover:bg-neutral-50 transition-colors shrink-0 cursor-pointer"
            title="Export documentation as JSON"
        >
            <MdFileDownload size={18}/>
            Export to JSON
        </button>
    );
};

export default ExportButton;
