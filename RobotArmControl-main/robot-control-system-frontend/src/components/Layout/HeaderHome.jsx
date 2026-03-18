import React from "react";
import robotHandLogo from "../../assets/robot-hand-logo.png";

function HeaderHome({ onLoginClick }) {
  const handleNavClick = (sectionId) => {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  };

  return (
    <header className="w-full flex justify-between items-center px-20 py-4 bg-white border-b-2 border-gray-100 shadow-sm sticky top-0 z-40">
      
      {/* Logo */}
      <div className="flex items-center gap-3 group cursor-pointer hover:opacity-90 transition">
        <div className="w-20 h-20 rounded-2xl flex items-center justify-center relative overflow-hidden p-1 bg-white shadow-md">
          <img 
            src={robotHandLogo}
            alt="RoboArm Logo"
            className="w-full h-full object-contain"
          />
        </div>
        <div className="flex flex-col">
          <h1 className="text-2xl font-bold text-black leading-tight tracking-tight">
            RoboArm
          </h1>
          <span className="text-xs text-gray-600 font-semibold tracking-widest">
            CONTROL ROBOT ARM
          </span>
        </div>
      </div>

      {/* Navigation - optional */}
      <nav className="flex gap-8">
        <button 
          type="button" 
          onClick={() => handleNavClick("features")}
          className="text-gray-600 hover:text-black font-medium transition"
        >
          Features
        </button>
        <button 
          type="button" 
          onClick={() => handleNavClick("about")}
          className="text-gray-600 hover:text-black font-medium transition"
        >
          About
        </button>
        <button 
          type="button" 
          onClick={() => handleNavClick("contact")}
          className="text-gray-600 hover:text-black font-medium transition"
        >
          Contact
        </button>
      </nav>

      {/* Login button */}
      <button
        onClick={onLoginClick}
        className="bg-black text-white px-8 py-2.5 rounded-lg hover:bg-gray-800 transition font-semibold"
      >
        Login
      </button>

    </header>
  );
}

export default HeaderHome;