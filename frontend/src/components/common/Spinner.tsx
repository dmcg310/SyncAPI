import React from 'react';

interface SpinnerProps {
    className?: string;
}

const Spinner: React.FC<SpinnerProps> = ({className = 'h-8 w-8'}) => (
    <div className={`animate-spin rounded-full border-b-2 border-primary-600 ${className}`}/>
);

export default Spinner;
