interface InputFieldProps<T extends string | number> {
  name: string;
  type: string;
  placeholder: string;
  state: T;
  setState: React.Dispatch<React.SetStateAction<T>>;
}

function FormInputField<T extends string | number>({
  name,
  type,
  placeholder,
  state,
  setState,
}: InputFieldProps<T>) {
  return (
    <div className="flex flex-col mb-6">
      <label
        htmlFor={name.toLowerCase()}
        className="mb-2 text-gray-300 font-semibold"
      >
        {name}
      </label>
      <input
        type={type}
        id={name.toLowerCase()}
        name={name.toLowerCase()}
        placeholder={placeholder}
        className="bg-gray-800 text-white border border-gray-700 rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition"
        onChange={(e) =>
          setState(
            type === "number"
              ? (Number(e.target.value) as T)
              : (e.target.value as T),
          )
        }
        value={state}
      />
    </div>
  );
}

export default FormInputField;
