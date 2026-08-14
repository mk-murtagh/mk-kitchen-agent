import { useState } from "react";
import { useZxing } from "react-zxing";

type Product = {
    barcode: string;
    name: string;
    brand: string;
    quantity: string;
    imageUrl: string;
    found: boolean;
};
type PantryForm = {
  name: string;
  quantity: string;
  unit: string;
  location: string;
  expirationDate: string;
};

export default function BarcodeScanner() {
    const [barcode, setBarcode] = useState<string>();
    const [lastScannedBarcode, setLastScannedBarcode] =
    useState<string | null>(null);
    const [product, setProduct] = useState<Product | null>(null);

    const [form, setForm] = useState<PantryForm>({
        name: "",
        quantity: "",
        unit: "",
        location: "pantry",
        expirationDate: "",
    });

    const [message, setMessage] = useState("");

    async function lookupBarcode(barcode: string) {
        const response = await fetch(
            `http://localhost:8080/api/products/barcode/${barcode}`
        );

        const data: Product = await response.json();
        setProduct(data);
        if (data.found) {
            setForm({
                name: data.name || "",
                quantity: "",
                unit: "",
                location: "pantry",
                expirationDate: "",
            });
        }
    }

    async function handleBarcode(barcode: string) {
        if (barcode === lastScannedBarcode) {
            return;
        }

        setLastScannedBarcode(barcode);
        setBarcode(barcode);
        setMessage("");

        await lookupBarcode(barcode);
    }

    async function addToPantry() {
        const response = await fetch(
        "http://localhost:8080/api/ingredients",
        {
            method: "POST",
            headers: {
            "Content-Type": "application/json",
            },
            body: JSON.stringify({
            name: form.name,
            quantity: Number(form.quantity),
            unit: form.unit,
            location: form.location,
            expirationDate:
                form.expirationDate || null,
            barcode: barcode,
            }),
        }
        );

        if (!response.ok) {
        setMessage("Could not add product to pantry.");
        return;
        }

        setMessage("Added to pantry!");
    }

    function handleInputChange(
        event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
    ) {
        const { name, value } = event.target;

        setForm((previousForm) => ({
        ...previousForm,
        [name]: value,
        }));
    }

    const { ref } = useZxing({
        onDecodeResult(result) {
            handleBarcode(result.rawValue);
        },
    });

    return (
        <div>
            <h2>Scan Product</h2>

            <video ref={ref} />

            {barcode && <p>Barcode: {barcode}</p>}

            {product && !product.found && (<p>Product not found.</p>)}

            {product && product.found && (
            <div>
                <h3>{product.name}</h3>

                {product.imageUrl && (
                <img
                    src={product.imageUrl}
                    alt={product.name}
                    width="200"
                />
                )}

                <p>Brand: {product.brand}</p>

                <h3>Add to Pantry</h3>
                
                <div>
                    <label>
                    Name
                    <input
                        name="name"
                        value={form.name}
                        onChange={handleInputChange}
                    />
                    </label>
                </div>

                <div>
                    <label>
                    Quantity
                    <input
                        name="quantity"
                        type="number"
                        step="0.01"
                        value={form.quantity}
                        onChange={handleInputChange}
                    />
                    </label>
                </div>

                <div>
                    <label>
                    Unit
                    <select
                        name="unit"
                        value={form.unit}
                        onChange={handleInputChange}
                    >
                        <option value="">Select unit</option>
                        <option value="piece">piece</option>
                        <option value="oz">oz</option>
                        <option value="lb">lb</option>
                        <option value="g">g</option>
                        <option value="kg">kg</option>
                        <option value="cup">cup</option>
                        <option value="ml">ml</option>
                        <option value="l">liter</option>
                    </select>
                    </label>
                </div>

                <div>
                    <label>
                    Location
                    <select
                        name="location"
                        value={form.location}
                        onChange={handleInputChange}
                    >
                        <option value="pantry">Pantry</option>
                        <option value="fridge">Fridge</option>
                        <option value="freezer">Freezer</option>
                    </select>
                    </label>
                </div>

                <div>
                    <label>
                    Expiration Date
                    <input
                        name="expirationDate"
                        type="date"
                        value={form.expirationDate}
                        onChange={handleInputChange}
                    />
                    </label>
                </div>

                <button onClick={addToPantry}>
                    Add to Pantry
                </button>

                {message && (
                    <p>{message}</p>
                )}
                    </div>
                )}
        </div>
    );
}