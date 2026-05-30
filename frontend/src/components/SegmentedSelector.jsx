export default function SegmentedSelector({ value, onChange, options }) {
  return (
    <div className="segmented">
      {options.map(opt => (
        <button
          key={opt.value}
          type="button"
          className={`segment${value === opt.value ? ' active' : ''}`}
          onClick={() => onChange(opt.value)}
        >
          {opt.label}
        </button>
      ))}
    </div>
  );
}
