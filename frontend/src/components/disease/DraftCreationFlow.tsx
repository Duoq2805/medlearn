import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ChooseMethodModal from './ChooseMethodModal';
import UploadDocumentView from './UploadDocumentView';
import ImportUrlView from './ImportUrlView';
import AiGenerateView from './AiGenerateView';
import DraftPreview from './DraftPreview';
import type {
  DraftCreationMethod,
  DraftCreationMetadata,
  DraftGenerationResponse,
  DiseaseDraftResponse,
} from '../../types/diseaseDraft';

interface DraftCreationFlowProps {
  open: boolean;
  onClose: () => void;
}

type FlowStep = 'choose-method' | 'upload' | 'import-url' | 'ai-generate' | 'preview';

export default function DraftCreationFlow({ open, onClose }: DraftCreationFlowProps) {
  const navigate = useNavigate();
  const [step, setStep] = useState<FlowStep>('choose-method');
  const [metadata, setMetadata] = useState<DraftCreationMetadata | null>(null);
  const [generatedDraft, setGeneratedDraft] = useState<DraftGenerationResponse | null>(null);

  const reset = () => {
    setStep('choose-method');
    setMetadata(null);
    setGeneratedDraft(null);
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  const handleMethodSelect = (method: DraftCreationMethod) => {
    switch (method) {
      case 'manual':
        handleClose();
        navigate('/diseases/new');
        break;
      case 'upload':
        setStep('upload');
        break;
      case 'import-url':
        setStep('import-url');
        break;
      case 'ai-generate':
        setStep('ai-generate');
        break;
    }
  };

  const handleDocumentUploaded = (document: import("../../api/document").Document) => {
    setMetadata({
      method: 'upload',
      sourceLabel: document.fileName,
      originalFilename: document.fileName,
    });
    // For now, skip to editor since no backend
    // In future: extract text -> generate draft -> preview
    handleClose();
    navigate('/diseases/new');
  };

  const handleDocumentImported = (document: import("../../api/document").Document) => {
    setMetadata({
      method: 'import-url',
      sourceLabel: document.title || document.fileName,
      sourceUrl: document.sourceUrl || undefined,
    });
    handleClose();
    navigate('/diseases/new');
  };

  const handleDraftGenerated = (response: DiseaseDraftResponse) => {
    handleClose();
    navigate('/drafts');
  };

  const handleAcceptDraft = () => {
    if (!metadata || !generatedDraft) return;
    handleClose();
    // Pass generated draft data to editor via navigation state
    navigate('/diseases/new', {
      state: {
        draftMetadata: metadata,
        draftResponse: generatedDraft,
      },
    });
  };

  const handleRejectDraft = () => {
    reset();
    setStep('choose-method');
  };

  if (!open) return null;

  // Wrap each step in the fullscreen modal backdrop
  const renderStep = () => {
    switch (step) {
      case 'choose-method':
        return (
          <ChooseMethodModal
            open={true}
            onClose={handleClose}
            onSelect={handleMethodSelect}
          />
        );

      case 'upload':
        return (
          <div className="fixed inset-0 z-50 flex items-center justify-center">
            <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={() => setStep('choose-method')} />
            <div className="relative w-full max-w-lg mx-4 card-neumorphic p-6 max-h-[80vh] overflow-y-auto">
              <UploadDocumentView
                onBack={() => setStep('choose-method')}
                onDocumentUploaded={handleDocumentUploaded}
              />
            </div>
          </div>
        );

      case 'import-url':
        return (
          <div className="fixed inset-0 z-50 flex items-center justify-center">
            <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={() => setStep('choose-method')} />
            <div className="relative w-full max-w-lg mx-4 card-neumorphic p-6 max-h-[80vh] overflow-y-auto">
              <ImportUrlView
                onBack={() => setStep('choose-method')}
                onDocumentImported={handleDocumentImported}
              />
            </div>
          </div>
        );

      case 'ai-generate':
        return (
          <div className="fixed inset-0 z-50 flex items-center justify-center">
            <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={() => setStep('choose-method')} />
            <div className="relative w-full max-w-2xl mx-4 card-neumorphic p-6 max-h-[80vh] overflow-y-auto">
              <AiGenerateView
                onBack={() => setStep('choose-method')}
                onDraftGenerated={handleDraftGenerated}
              />
            </div>
          </div>
        );

      case 'preview':
        return generatedDraft && metadata ? (
          <div className="fixed inset-0 z-50 flex items-center justify-center">
            <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" />
            <div className="relative w-full max-w-2xl mx-4 max-h-[85vh] overflow-y-auto">
              <DraftPreview
                response={generatedDraft}
                metadata={metadata}
                onAccept={handleAcceptDraft}
                onReject={handleRejectDraft}
              />
            </div>
          </div>
        ) : null;

      default:
        return null;
    }
  };

  return renderStep();
}

