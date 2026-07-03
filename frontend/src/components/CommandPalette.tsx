import React, { useState, useEffect, useRef, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Search, X, ChevronRight } from 'lucide-react';

interface Command {
  id: string;
  name: string;
  description: string;
  shortcut?: string;
  run: () => void;
}

interface CommandPaletteProps {
  commands: Command[];
}

const CommandPalette: React.FC<CommandPaletteProps> = ({ commands }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [query, setQuery] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);
  const [filteredCommands, setFilteredCommands] = useState<Command[]>(commands);

  const openPalette = useCallback(() => {
    setIsOpen(true);
    requestAnimationFrame(() => inputRef.current?.focus());
  }, []);

  const closePalette = useCallback(() => {
    setIsOpen(false);
    setQuery('');
  }, []);

  useEffect(() => {
    const down = (e: KeyboardEvent) => {
      if (e.key === 'k' && (e.metaKey || e.ctrlKey)) {
        e.preventDefault();
        openPalette();
      }
    };
    document.addEventListener('keydown', down);
    return () => document.removeEventListener('keydown', down);
  }, [openPalette]);

  useEffect(() => {
    if (isOpen) {
      const down = (e: KeyboardEvent) => {
        if (e.key === 'Escape') {
          closePalette();
        }
      };
      document.addEventListener('keydown', down);
      return () => document.removeEventListener('keydown', down);
    }
  }, [isOpen, closePalette]);

  useEffect(() => {
    setFilteredCommands(
      commands.filter(cmd =>
        cmd.name.toLowerCase().includes(query.toLowerCase()) ||
        cmd.description.toLowerCase().includes(query.toLowerCase())
      )
    );
  }, [query, commands]);

  const handleCommandClick = (command: Command) => {
    command.run();
    closePalette();
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          className="fixed inset-0 z-[100] flex items-center justify-center p-4"
          style={{ backgroundColor: 'rgba(0, 0, 0, 0.7)' }} // Semi-transparent overlay
          onClick={closePalette} // Close if clicking outside
        >
          <motion.div
            className="relative w-full max-w-2xl bg-[var(--bg-primary)] p-6 rounded-xl shadow-2xl border border-[var(--border)]"
            onClick={e => e.stopPropagation()} // Prevent clicks inside from closing palette
            initial={{ opacity: 0, y: 20, scale: 0.98 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 20, scale: 0.98 }}
            transition={{ duration: 0.25, ease: 'easeOut' }}
          >
            <div className="relative mb-4">
              <Search size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" />
              <input
                ref={inputRef}
                type="text"
                placeholder="Type a command or search..."
                value={query}
                onChange={e => setQuery(e.target.value)}
                className="input-neumorphic w-full pl-12 pr-10 py-3 text-base"
              />
              <button
                onClick={closePalette}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)] hover:text-[var(--text-primary)]"
                aria-label="Close command palette"
              >
                <X size={16} />
              </button>
            </div>

            <div className="max-h-[400px] overflow-y-auto scrollbar-thin scrollbar-thumb-[var(--accent-primary)]/20 scrollbar-track-[var(--bg-primary)]">
              {filteredCommands.length > 0 ? (
                filteredCommands.map(cmd => (
                  <button
                    key={cmd.id}
                    onClick={() => handleCommandClick(cmd)}
                    className="w-full text-left flex items-center gap-3 p-3 rounded-lg hover:bg-[var(--surface-hover)] transition-colors mb-1"
                  >
                    <div className="flex items-center gap-2">
                      <div className="w-8 h-8 rounded-lg bg-[var(--accent-primary)]/10 flex items-center justify-center">
                        <ChevronRight size={16} className="text-[var(--accent-primary)]" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-[var(--text-primary)] truncate">{cmd.name}</p>
                        <p className="text-xs text-[var(--text-tertiary)] truncate">{cmd.description}</p>
                      </div>
                    </div>
                    {cmd.shortcut && (
                      <span className="ml-auto text-xs text-[var(--text-tertiary)] px-2 py-1 rounded-md bg-[var(--surface-secondary)]">
                        {cmd.shortcut}
                      </span>
                    )}
                  </button>
                ))
              ) : (
                <div className="text-center py-8 text-[var(--text-secondary)]">
                  No commands found.
                </div>
              )}
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default CommandPalette;
