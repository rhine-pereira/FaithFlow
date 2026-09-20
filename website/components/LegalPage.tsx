import { ReactNode } from "react";
import SiteHeader from "./SiteHeader";
import SiteFooter from "./SiteFooter";

export default function LegalPage({
  title,
  updated,
  children,
}: {
  title: string;
  updated: string;
  children: ReactNode;
}) {
  return (
    <div className="flex flex-col flex-1">
      <SiteHeader />
      <main className="flex-1">
        <div className="max-w-2xl mx-auto px-6 py-16">
          <h1 className="font-serif text-4xl tracking-tight mb-2">{title}</h1>
          <p className="text-xs uppercase tracking-widest text-muted mb-12">
            Last updated: {updated}
          </p>
          <div className="legal-content flex flex-col gap-6 text-sm leading-relaxed text-foreground/90">
            {children}
          </div>
        </div>
      </main>
      <SiteFooter />
    </div>
  );
}
