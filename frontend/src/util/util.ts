import {METHOD_COLORS} from "../util/constants.ts";

export const getMethodColorClass = (method: string) => {
    return METHOD_COLORS[method as keyof typeof METHOD_COLORS] || 'bg-neutral-500';
};

export const getStatusColor = (status: string): string => {
    const statusNum = parseInt(status);
    if (statusNum >= 200 && statusNum < 300) {
        return 'bg-green-100 text-green-700';
    }

    if (statusNum >= 300 && statusNum < 400) {
        return 'bg-blue-100 text-blue-700';
    }

    if (statusNum >= 400 && statusNum < 500) {
        return 'bg-amber-100 text-amber-700';
    }

    if (statusNum >= 500) {
        return 'bg-error-100 text-error-700';
    }

    return 'bg-neutral-100 text-neutral-700';
};
