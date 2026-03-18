import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "../../api/authApi";
import { getDefaultAdminPath } from "../../utils/auth";

function LoginModal({ isOpen, onClose }) {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: "",
    password: "",
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState("");
  const [showForgotPassword, setShowForgotPassword]= useState(false);

  if (!isOpen) return null;

  // handle input change
  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm({
      ...form,
      [name]: value,
    });

    // clear error khi user nhập lại
    setErrors({
      ...errors,
      [name]: "",
    });

    setServerError("");
  };

  // validate form
  const validate = () => {
    const newErrors = {};

    if (!form.username.trim()) {
      newErrors.username = "Please enter username";
    }

    if (!form.password.trim()) {
      newErrors.password = "Please enter password";
    }

    setErrors(newErrors);

    return Object.keys(newErrors).length === 0;
  };

  // login
  const handleLogin = async () => {
    if (!validate()) return;

    try {
      setLoading(true);

      const authData = await authApi.login({
        username: form.username,
        password: form.password,
      });

      navigate(getDefaultAdminPath(authData?.role), { replace: true });
    } catch (error) {
      console.error(error);
      setServerError("Incorrect username or password");
    } finally {
      setLoading(false);
    }
  };

  // enter key login
  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !loading) {
      handleLogin();
    }
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/50 backdrop-blur-sm z-50">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden">

        {/* Header */}
        <div className="bg-gradient-to-r from-gray-900 to-black text-white p-8">
          <h2 className="text-3xl font-bold mb-2">Welcome Back</h2>
          <p className="text-gray-300">Sign in to your account</p>
        </div>

        {/* Form */}
        <div className="p-8">

          {/* Server error */}
          {serverError && (
            <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-600 text-sm font-medium">
                {serverError}
              </p>
            </div>
          )}

          <div className="space-y-5">

            {/* Username */}
            <div>
              <label className="block text-sm font-semibold text-black mb-2">
                Username
              </label>

              <input
                name="username"
                type="text"
                placeholder="Enter your username"
                value={form.username}
                onChange={handleChange}
                onKeyDown={handleKeyDown}
                disabled={loading}
                className={`w-full px-4 py-3 border-2 rounded-lg focus:outline-none transition
                  ${errors.username
                    ? "border-red-500 focus:border-red-500"
                    : "border-gray-200 focus:border-black"
                  }`}
              />

              {errors.username && (
                <p className="text-red-500 text-sm mt-1">
                  {errors.username}
                </p>
              )}
            </div>

            {/* Password */}
            <div>
              <label className="block text-sm font-semibold text-black mb-2">
                Password
              </label>

              <input
                name="password"
                type="password"
                placeholder="Enter your password"
                value={form.password}
                onChange={handleChange}
                onKeyDown={handleKeyDown}
                disabled={loading}
                className={`w-full px-4 py-3 border-2 rounded-lg focus:outline-none transition
                  ${errors.password
                    ? "border-red-500 focus:border-red-500"
                    : "border-gray-200 focus:border-black"
                  }`}
              />

              {errors.password && (
                <p className="text-red-500 text-sm mt-1">
                  {errors.password}
                </p>
              )}
            <button
              onClick={() => setShowForgotPassword(true)}
              className="text-sm text-gray-500 hover:text-black mt-2 font medium transition"
            >
              Forgot Password?
            </button>

            </div>

          </div>

          {/* Login button */}
          <button
            onClick={handleLogin}
            disabled={loading}
            className="w-full mt-6 bg-black text-white py-3 rounded-lg hover:bg-gray-800 disabled:opacity-60 disabled:cursor-not-allowed transition font-semibold text-lg"
          >
            {loading ? "Signing in..." : "Sign In"}
          </button>

          {/* Close */}
          <button
            onClick={onClose}
            disabled={loading}
            className="mt-3 w-full text-gray-500 hover:text-gray-700 text-sm font-medium transition py-2"
          >
            Close
          </button>
        </div>

        {/* Footer */}
        <div className="bg-gray-50 px-8 py-4 text-center border-t border-gray-200">
          <p className="text-gray-600 text-sm">
            Need help?{" "}
            <button className="text-black font-semibold hover:underline">
              Contact support
            </button>
          </p>
        </div>

      </div>
      
      {/* Forgot Password Modal */}
      {showForgotPassword && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8">
            <div className="flex items-start justify-between mb-4">
              <h3 className="text-2xl font-bold text-black">Password Reset</h3>
              <button
                type="button"
                onClick={() => setShowForgotPassword(false)}
                className="text-gray-400 hover:text-black text-2xl"
              >
                ✕
              </button>
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
              <p className="text-blue-900 text-sm font-medium">
                To reset your password, please contact your administrator.
              </p>
            </div>

            <button
              type="button"
              onClick={() => setShowForgotPassword(false)}
              className="w-full bg-black text-white py-3 rounded-lg hover:bg-gray-800 transition font-semibold"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default LoginModal;