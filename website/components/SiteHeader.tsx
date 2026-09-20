import Image from "next/image";
import Link from "next/link";

const PLAY_STORE_URL =
  "https://play.google.com/store/apps/details?id=com.rhinepereira.faithflow";

export default function SiteHeader() {
  return (
    <header className="border-b border-border">
      <div className="flex items-center justify-between max-w-5xl w-full mx-auto px-6 py-5">
        <Link href="/" className="flex items-center gap-2.5">
          <Image
            src="/faithflow-icon.png"
            alt="FaithFlow"
            width={28}
            height={28}
            className="rounded-md"
          />
          <span className="font-serif text-[1.15rem] tracking-tight">
            FaithFlow
          </span>
        </Link>
        <nav className="flex items-center gap-8 text-sm">
          <Link
            href="/#features"
            className="text-muted hover:text-foreground transition-colors"
          >
            Features
          </Link>
          <a
            href={PLAY_STORE_URL}
            className="text-foreground border-b border-foreground/40 hover:border-foreground pb-0.5 transition-colors"
          >
            Download
          </a>
        </nav>
      </div>
    </header>
  );
}
