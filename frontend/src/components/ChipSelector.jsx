export default function ChipSelector({ value, onChange, options }) {
  function toggle(v) {
    onChange(value.includes(v) ? value.filter(x => x !== v) : [...value, v]);
  }

  return (
    <div className="chips">
      {options.map(opt => (
        <button
          key={opt.value}
          type="button"
          className={`chip${value.includes(opt.value) ? ' active' : ''}`}
          onClick={() => toggle(opt.value)}
        >
          {opt.label}
        </button>
      ))}
    </div>
  );
}
