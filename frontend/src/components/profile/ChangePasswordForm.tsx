import React, {useState} from 'react';
import {authApi} from '../../services/api';
import {getErrorMessage} from '../../util/errors';
import {FaExchangeAlt} from 'react-icons/fa';
import {MIN_PASSWORD_LENGTH} from "../../util/constants.ts";

interface PasswordFormData {
    originalPassword: string;
    newPassword: string;
    confirmPassword: string;
}

interface ValidationErrors {
    originalPassword?: string;
    newPassword?: string;
    confirmPassword?: string;
}

const ChangePasswordForm: React.FC = () => {
    const [formData, setFormData] = useState<PasswordFormData>({
        originalPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    const [errors, setErrors] = useState<ValidationErrors>({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');

    const validateForm = (): boolean => {
        const newErrors: ValidationErrors = {};

        if (!formData.originalPassword) {
            newErrors.originalPassword = 'Original password is required';
        }

        if (!formData.newPassword) {
            newErrors.newPassword = 'New password is required';
        } else if (formData.newPassword.length < MIN_PASSWORD_LENGTH) {
            newErrors.newPassword = `Password must be at least ${MIN_PASSWORD_LENGTH} characters`;
        } else if (formData.newPassword === formData.originalPassword) {
            newErrors.newPassword = 'New password must be different from original password';
        }

        if (!formData.confirmPassword) {
            newErrors.confirmPassword = 'Please confirm your password';
        } else if (formData.confirmPassword !== formData.newPassword) {
            newErrors.confirmPassword = 'Passwords do not match';
        }

        setErrors(newErrors);

        return Object.keys(newErrors).length === 0;
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setFormData(prev => ({...prev, [name]: value}));

        if (errors[name as keyof ValidationErrors]) {
            setErrors(prev => ({...prev, [name]: undefined}));
        }

        setSuccessMessage('');
        setErrorMessage('');
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSuccessMessage('');
        setErrorMessage('');

        if (!validateForm()) {
            return;
        }

        setIsSubmitting(true);

        try {
            await authApi.updatePassword({
                originalPassword: formData.originalPassword,
                newPassword: formData.newPassword
            });

            setSuccessMessage('Password updated successfully');
            setFormData({
                originalPassword: '',
                newPassword: '',
                confirmPassword: ''
            });
        } catch (error: unknown) {
            const message = getErrorMessage(error, 'Failed to update password. Please check your original password.');
            setErrorMessage(message);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="mb-8">
            <h2 className="text-lg font-medium text-neutral-900 mb-4">Security</h2>

            {successMessage && (
                <div className="bg-success-50 text-success-700 px-4 py-3 rounded-lg text-md mb-4">
                    {successMessage}
                </div>
            )}

            {errorMessage && (
                <div className="bg-error-50 text-error-700 px-4 py-3 rounded-lg text-md mb-4">
                    {errorMessage}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label htmlFor="originalPassword" className="block text-md font-medium text-neutral-700 mb-1">
                        Original Password
                    </label>
                    <input
                        type="password"
                        id="originalPassword"
                        name="originalPassword"
                        value={formData.originalPassword}
                        onChange={handleChange}
                        className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                            errors.originalPassword
                                ? 'border-error-500 focus:ring-error-500'
                                : 'border-neutral-300 focus:ring-primary-500'
                        }`}
                        disabled={isSubmitting}
                        required
                    />
                    {errors.originalPassword && (
                        <p className="mt-1 text-sm text-error-600">{errors.originalPassword}</p>
                    )}
                </div>

                <div>
                    <label htmlFor="newPassword" className="block text-md font-medium text-neutral-700 mb-1">
                        New Password
                    </label>
                    <input
                        type="password"
                        id="newPassword"
                        name="newPassword"
                        value={formData.newPassword}
                        onChange={handleChange}
                        className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                            errors.newPassword
                                ? 'border-error-500 focus:ring-error-500'
                                : 'border-neutral-300 focus:ring-primary-500'
                        }`}
                        disabled={isSubmitting}
                        required
                    />
                    {errors.newPassword && (
                        <p className="mt-1 text-sm text-error-600">{errors.newPassword}</p>
                    )}
                    <p className="mt-1 text-sm text-neutral-500">Minimum {MIN_PASSWORD_LENGTH} characters</p>
                </div>

                <div>
                    <label htmlFor="confirmPassword" className="block text-md font-medium text-neutral-700 mb-1">
                        Confirm New Password
                    </label>
                    <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                            errors.confirmPassword
                                ? 'border-error-500 focus:ring-error-500'
                                : 'border-neutral-300 focus:ring-primary-500'
                        }`}
                        disabled={isSubmitting}
                        required
                    />
                    {errors.confirmPassword && (
                        <p className="mt-1 text-sm text-error-600">{errors.confirmPassword}</p>
                    )}
                </div>

                <div className="pt-2">
                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium disabled:bg-neutral-400 disabled:cursor-not-allowed cursor-pointer"
                    >
                        {isSubmitting ? 'Updating...' : 'Change Password'}
                        <FaExchangeAlt size={20}/>
                    </button>
                </div>
            </form>
        </div>
    );
};

export default ChangePasswordForm;
