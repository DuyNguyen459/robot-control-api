import React, { useState } from "react";
import HeaderHome from "../../components/Layout/HeaderHome";
import LoginModal from "../../components/login/LoginModal";
import robot from "../../assets/robot-banner.jpg";   

function HomePage() {

  const [showLogin, setShowLogin] = useState(() => {
    try {
      const params = new URLSearchParams(globalThis.location.search);
      return params.get("login") === "1";
    } catch {
      return false;
    }
  });

  const features = [
    {
      title: "Real-time Monitoring",
      description: "Monitor all robot arms and devices in real-time from a centralized dashboard"
    },
    {
      title: "Factory Management",
      description: "Organize and manage multiple factories with complete control over operations"
    },
    {
      title: "Advanced Control",
      description: "Send commands and control robot arms with precision and safety"
    },
    {
      title: "Secure Access",
      description: "Role-based access control ensures data security and user management"
    }
  ];

  const steps = [
    {
      number: "01",
      title: "Connect Your Devices",
      description: "Easily integrate your robot arms and IoT devices with our platform"
    },
    {
      number: "02",
      title: "Configure Settings",
      description: "Set up factories, areas, hubs, and device parameters in minutes"
    },
    {
      number: "03",
      title: "Monitor & Control",
      description: "Start monitoring real-time data and control robots instantly"
    },
    {
      number: "04",
      title: "Optimize Performance",
      description: "Use analytics to improve efficiency and reduce downtime"
    }
  ];

  const testimonials = [
    {
      company: "SkyTech Factory",
      quote: "This platform transformed how we manage our robot operations. Highly recommended!",
      author: "Nguyễn Anh Đức"
    },
    {
      company: "Prime Manufacturing",
      quote: "Best investment for our automation. ROI within 6 months.",
      author: "Trần Quang Hùng"
    },
    {
      company: "Innovation Labs",
      quote: "Excellent support and intuitive interface. Worth every penny.",
      author: "Lê Minh Hoàng"
    },
    {
      company: "Global Robotics",
      quote: "Reliable, scalable, and easy to use. Perfect for large operations.",
      author: "Phạm Hoàng Yên"
    }
  ];

  return (
    <div className="w-full bg-white">

      <HeaderHome onLoginClick={() => setShowLogin(true)} />

      {/* Hero Banner */}
      <section className="w-full bg-gradient-to-r from-gray-900 to-black text-white py-24 px-20">
        <div className="flex w-full items-center gap-16">
          {/* Text left */}
          <div className="flex-1">
            <h1 className="text-6xl font-bold mb-6 leading-tight">
              Robot Arm Control Platform
            </h1>
            <p className="text-xl text-gray-300 mb-8 leading-relaxed">
              Monitor and control industrial robot arms from a single unified dashboard. Streamline your factory operations with intelligent automation.
            </p>
            <button
              onClick={() => setShowLogin(true)}
              className="bg-white text-black px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition"
            >
              Get Started
            </button>
          </div>

          {/* Image right */}
          <div className="flex-1 flex justify-center">
            <img
              src={robot}
              className="w-full max-w-md object-cover rounded-lg shadow-2xl"
              alt="Robot Arm"
            />
          </div>
        </div>
      </section>

      {/* Features Grid - 4 Columns */}
      <section id="features" className="bg-white py-20 px-20 scroll-mt-20">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-black mb-4">
              Powerful Features
            </h2>
            <p className="text-xl text-gray-600">
              Everything you need to manage industrial robots efficiently
            </p>
          </div>

          <div className="grid grid-cols-4 gap-6">
            {features.map((feature, idx) => (
              <div
                key={feature.title}
                className="p-8 border-2 border-gray-200 rounded-xl hover:border-black hover:shadow-lg transition"
              >
                <div className="w-12 h-12 bg-black rounded-lg mb-4 flex items-center justify-center">
                  <span className="text-white font-bold text-lg">{idx + 1}</span>
                </div>
                <h3 className="text-xl font-bold text-black mb-3">
                  {feature.title}
                </h3>
                <p className="text-gray-600 leading-relaxed">
                  {feature.description}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="bg-black text-white py-20 px-20">
        <div className="max-w-7xl mx-auto">
          <h2 className="text-4xl font-bold mb-12 text-center">
            Industry Leading Platform
          </h2>
          <div className="grid grid-cols-4 gap-8 text-center">
            <div>
              <div className="text-5xl font-bold text-white mb-2">500+</div>
              <p className="text-gray-400">Active Users</p>
            </div>
            <div>
              <div className="text-5xl font-bold text-white mb-2">50+</div>
              <p className="text-gray-400">Factories</p>
            </div>
            <div>
              <div className="text-5xl font-bold text-white mb-2">1000+</div>
              <p className="text-gray-400">Robot Arms</p>
            </div>
            <div>
              <div className="text-5xl font-bold text-white mb-2">99.9%</div>
              <p className="text-gray-400">Uptime</p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="bg-white py-16 px-20 text-center border-t-2 border-gray-200">
        <h2 className="text-3xl font-bold text-black mb-6">
          Ready to Transform Your Operations?
        </h2>
        <p className="text-gray-600 mb-8 text-lg">
          Join hundreds of factories already using our platform to optimize their robot operations
        </p>
        <button
          onClick={() => setShowLogin(true)}
          className="bg-black text-white px-10 py-4 rounded-lg font-semibold hover:bg-gray-800 transition text-lg"
        >
          Sign In Now
        </button>
      </section>

      {/* How It Works Section */}
      <section id="how-it-works" className="bg-black text-white py-20 px-20 scroll-mt-20">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold mb-4">How It Works</h2>
            <p className="text-xl text-gray-400">Get your robot operations up and running in 4 simple steps</p>
          </div>

          <div className="grid grid-cols-4 gap-6">
            {steps.map((step) => (
              <div key={step.number} className="relative">
                <div className="text-6xl font-bold text-gray-700 mb-4">{step.number}</div>
                <h3 className="text-xl font-bold mb-3">{step.title}</h3>
                <p className="text-gray-400 leading-relaxed">{step.description}</p>
                {Number.parseInt(step.number, 10) < 4 && (
                  <div className="hidden lg:block absolute top-12 -right-3 w-6 h-1 bg-white"></div>
                )}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Testimonials Section */}
      <section id="about" className="bg-white py-20 px-20 scroll-mt-20">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-black mb-4">What Our Clients Say</h2>
            <p className="text-xl text-gray-600">Trusted by leading factories worldwide</p>
          </div>

          <div className="grid grid-cols-2 gap-8">
            {testimonials.map((testimonial) => (
              <div
                key={testimonial.company}
                className="p-8 border-2 border-gray-200 rounded-xl hover:border-black transition hover:shadow-lg"
              >
                <div className="flex items-center gap-1 mb-4">
                  <span className="text-black">★★★★★</span>
                </div>
                <p className="text-gray-700 text-lg mb-6 italic">"{testimonial.quote}"</p>
                <div className="border-t border-gray-200 pt-4">
                  <p className="font-bold text-black">{testimonial.author}</p>
                  <p className="text-gray-600 text-sm">{testimonial.company}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Technology Section */}
      <section className="bg-gray-50 py-20 px-20 border-t-2 border-gray-200">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-black mb-4">Built with Modern Technology</h2>
            <p className="text-xl text-gray-600">Enterprise-grade platform with cutting-edge technology</p>
          </div>

          <div className="grid grid-cols-3 gap-8">
            <div className="bg-white p-8 rounded-xl border-2 border-gray-200 text-center">
              <div className="text-4xl font-bold text-black mb-3">React.js</div>
              <p className="text-gray-600">Modern frontend framework for responsive UI</p>
            </div>
            <div className="bg-white p-8 rounded-xl border-2 border-gray-200 text-center">
              <div className="text-4xl font-bold text-black mb-3">REST API</div>
              <p className="text-gray-600">Scalable backend with secure API endpoints</p>
            </div>
            <div className="bg-white p-8 rounded-xl border-2 border-gray-200 text-center">
              <div className="text-4xl font-bold text-black mb-3">Cloud Ready</div>
              <p className="text-gray-600">Deploy anywhere with containerized architecture</p>
            </div>
          </div>
        </div>
      </section>

      {/* Security Section */}
      <section id="contact" className="bg-black text-white py-20 px-20 scroll-mt-20">
        <div className="max-w-7xl mx-auto">
          <div className="grid grid-cols-2 gap-12 items-center">
            <div>
              <h2 className="text-4xl font-bold mb-6">Enterprise Security</h2>
              <ul className="space-y-4">
                <li className="flex items-center gap-3">
                  <span className="text-2xl">✓</span>
                  <span className="text-lg">256-bit SSL Encryption</span>
                </li>
                <li className="flex items-center gap-3">
                  <span className="text-2xl">✓</span>
                  <span className="text-lg">Role-based Access Control</span>
                </li>
                <li className="flex items-center gap-3">
                  <span className="text-2xl">✓</span>
                  <span className="text-lg">Regular Security Audits</span>
                </li>
                <li className="flex items-center gap-3">
                  <span className="text-2xl">✓</span>
                  <span className="text-lg">Data Backup & Recovery</span>
                </li>
                <li className="flex items-center gap-3">
                  <span className="text-2xl">✓</span>
                  <span className="text-lg">GDPR Compliant</span>
                </li>
              </ul>
            </div>
            <div className="bg-gray-900 p-8 rounded-xl">
              <p className="text-gray-300 text-lg leading-relaxed">
                Your data security is our top priority. We implement industry-leading security practices to ensure your robot operations remain safe and protected 24/7.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Final CTA Section */}
      <section className="bg-white py-20 px-20 text-center">
        <h2 className="text-4xl font-bold text-black mb-4">Start Your Free Trial Today</h2>
        <p className="text-xl text-gray-600 mb-10">No credit card required. Full access for 30 days.</p>
        <div className="flex gap-4 justify-center">
          <button
            onClick={() => setShowLogin(true)}
            className="bg-black text-white px-10 py-4 rounded-lg font-semibold hover:bg-gray-800 transition text-lg"
          >
            Sign In
          </button>
          <button
            type="button"
            className="bg-white text-black border-2 border-black px-10 py-4 rounded-lg font-semibold hover:bg-gray-50 transition text-lg"
          >
            Learn More
          </button>
        </div>
      </section>

      <LoginModal
        isOpen={showLogin}
        onClose={() => setShowLogin(false)}
      />

    </div>
  );
}

export default HomePage;