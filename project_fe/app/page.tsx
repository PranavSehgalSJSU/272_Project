import Image from "next/image";
import { redirect } from "next/navigation";

export default function Home() {
redirect("/auth/login"); // or "/auth/login" if your page lives there
return null;
}