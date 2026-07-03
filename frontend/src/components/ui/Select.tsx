import React, { useState } from 'react';

interface SelectProps {
  options: { value: string | null; label: string }[];
  placeholder?: string;
  selectedOption: { value: string | null; label: string } | null;
  onSelect: (value: string | null) => void;
  className?: string;
}

export default function Select({
  options,
  placeholder,
  selectedOption,
  onSelect,
  className = '',
}: SelectProps) {
  const [isOpen, setIsOpen] = useState(false);

  const toggleDropdown = () => setIsOpen(!isOpen);

  const handleSelect = (option: { value: string | null; label: string }) => {
    onSelect(option.value);
    setIsOpen(false);
  };

  return (
    <div className={`relative ${className}`}>
      <button
        onClick={toggleDropdown}
        className="btn-neumorphic-secondary w-full py-3 px-4 text-left flex justify-between items-center input-neumorphic"
      >
        {selectedOption ? selectedOption.label : placeholder}
        {isOpen ? (
          <span className="transform rotate-180 transition-transform duration-300">▲</span>
        ) : (
          <span>▼</span>
        )}
      </button>

      {isOpen && (
        <div className="absolute top-full left-0 right-0 mt-2 card-neumorphic-sm z-10">
          <div className="max-h-48 overflow-y-auto">
            {options.map((option) => (
              <div
                key={option.value || 'null'}
                onClick={() => handleSelect(option)}
                className="px-4 py-3 cursor-pointer hover:bg-[rgba(140,176,77,0.1)] dark:hover:bg-[rgba(140,176,77,0.05)] transition-colors"
              >
                {option.label}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
