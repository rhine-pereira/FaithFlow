import SiteHeader from "@/components/SiteHeader";
import SiteFooter from "@/components/SiteFooter";

const PLAY_STORE_URL =
  "https://play.google.com/store/apps/details?id=com.rhinepereira.faithflow";

function Icon({ path, className = "w-5 h-5" }: { path: string; className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.4"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
      aria-hidden="true"
    >
      <path d={path} />
    </svg>
  );
}

const ICONS = {
  book: "M4 5.5C4 4.67 4.67 4 5.5 4H12v16H5.5A1.5 1.5 0 0 1 4 18.5v-13ZM20 5.5c0-.83-.67-1.5-1.5-1.5H12v16h6.5c.83 0 1.5-.67 1.5-1.5v-13Z",
  share: "M8.5 10.5 15 6M8.5 13.5 15 18M6 14a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5ZM17.5 8a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5ZM17.5 21a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5Z",
  pencil: "m5 19 1-4L16.5 4.5a1.5 1.5 0 0 1 2 0l1 1a1.5 1.5 0 0 1 0 2L9 18l-4 1Zm10.5-13L18 8.5",
  flame: "M12 3c1 3-3 4.5-3 8a3 3 0 0 0 6 0c0-1.5-1-2-1-3 1.5 1 2.5 3 2.5 5a5.5 5.5 0 1 1-11 0C5.5 8.5 9 7 12 3Z",
  cloud: "M7 18a4 4 0 0 1-.5-7.97A5 5 0 0 1 16.2 8.1 4.5 4.5 0 0 1 17 17H7Z",
  check: "m5 12.5 4 4 10-10",
  arrowDown: "M12 5v14M6 13l6 6 6-6",
  search: "M11 4a7 7 0 1 0 0 14 7 7 0 0 0 0-14ZM21 21l-4.35-4.35",
};

const THEMES = [
  { name: "Comfort in Trials", count: 12 },
  { name: "Morning Prayers", count: 8 },
  { name: "Hope & Promises", count: 15 },
];

const NOTES = [
  { title: "Sunday reflection", body: "Grateful for a quiet start to the week.", ref: "Psalm 23:1" },
  { title: "Prayer list", body: "For patience with the kids, for Dad's recovery." },
  { title: "On forgiveness", body: "Re-reading this after today's conversation.", ref: "Colossians 3:13" },
  { title: "Gratitude, day 4", body: "Small things: coffee, sunlight, a good sleep." },
];

const CALENDAR_DAYS = [
  1, 1, 1, 1, 1, 0, 1,
  1, 1, 0, 1, 1, 1, 1,
  1, 2, 1, 1, 1, 1, 1,
];

const SHOWCASE = [
  {
    eyebrow: "Verse Themes",
    title: "Give your verses a home, not a pile",
    description:
      "Create themed collections, reorder them by hand, and open any theme to find exactly the verses you saved there — even with no signal.",
    icon: ICONS.book,
    mock: (
      <div className="rounded-2xl border border-border bg-card overflow-hidden">
        <div className="flex items-center justify-between px-5 pt-5 pb-4">
          <span className="font-serif text-base">Your Themes</span>
          <span className="text-xs text-accent">+ New</span>
        </div>
        <div className="border-t border-border">
          {THEMES.map((theme, i) => (
            <div
              key={theme.name}
              className={`flex items-center justify-between px-5 py-4 ${
                i > 0 ? "border-t border-border" : ""
              }`}
            >
              <div className="flex items-center gap-3">
                <span className="w-1.5 h-1.5 rounded-full bg-accent" />
                <span className="text-sm">{theme.name}</span>
              </div>
              <span className="text-xs text-muted">{theme.count} verses</span>
            </div>
          ))}
        </div>
      </div>
    ),
  },
  {
    eyebrow: "Verse Auto-Complete",
    title: "Type a reference, we'll add the verse",
    description:
      "Type a reference like John 3:16, a range like John 3:16-18, or a list like John 3:16,17,20 — FaithFlow looks it up in our local Bible database and adds the full text instantly. Supports RSV today, with more translations on the way.",
    icon: ICONS.search,
    mock: (
      <div className="rounded-2xl border border-border bg-card p-5 space-y-4">
        <div className="rounded-lg border border-border px-3 py-2 text-sm flex items-center gap-2">
          <Icon path={ICONS.search} className="w-4 h-4 text-muted shrink-0" />
          John 3:16-18
        </div>
        <div className="rounded-lg border border-border p-4 flex flex-col gap-2.5">
          {[
            "For God so loved the world, that he gave his only Son, that whoever believes in him should not perish but have eternal life.",
            "For God sent the Son into the world, not to condemn the world, but that the world might be saved through him.",
            "He who believes in him is not condemned; he who does not believe is condemned already.",
          ].map((verse, i) => (
            <p
              key={i}
              className="font-serif italic text-sm leading-snug"
            >
              <sup className="not-italic text-[10px] text-accent mr-1">
                {16 + i}
              </sup>
              &ldquo;{verse}&rdquo;
            </p>
          ))}
          <div className="mt-1 flex items-center justify-between">
            <p className="text-[11px] uppercase tracking-wide text-accent">
              John 3:16-18
            </p>
            <span className="text-[10px] rounded-full border border-border px-2 py-0.5 text-muted">
              RSV
            </span>
          </div>
        </div>
      </div>
    ),
  },
  {
    eyebrow: "Smart Import",
    title: "Share it in, we'll clean it up",
    description:
      "Send a verse from your Bible app, a browser, or anywhere else. FaithFlow strips the version tag and the link, keeping just the reference and the text.",
    icon: ICONS.share,
    mock: (
      <div className="rounded-2xl border border-border bg-card p-5 space-y-4">
        <div className="rounded-lg border border-dashed border-border p-4">
          <p className="text-xs text-muted mb-2">Shared from Chrome</p>
          <p className="text-sm text-foreground/70 leading-relaxed">
            &ldquo;For God so loved the world&hellip;&rdquo; (NIV) —
            bible.com/john/3/16
          </p>
        </div>
        <div className="flex justify-center text-accent">
          <Icon path={ICONS.arrowDown} className="w-4 h-4" />
        </div>
        <div className="rounded-lg border border-border p-4">
          <p className="font-serif italic text-sm leading-snug">
            &ldquo;For God so loved the world, that he gave his only
            Son&hellip;&rdquo;
          </p>
          <p className="mt-2 text-[11px] uppercase tracking-wide text-accent">
            John 3:16
          </p>
        </div>
      </div>
    ),
  },
  {
    eyebrow: "Personal Notes",
    title: "Journal with scripture at your fingertips",
    description:
      "Write freely — FaithFlow detects Bible references as you type and links each one straight back to the verse it belongs to.",
    icon: ICONS.pencil,
    mock: (
      <div className="grid grid-cols-2 gap-3">
        {NOTES.map((note) => (
          <div
            key={note.title}
            className="rounded-xl border border-border bg-card p-4"
          >
            <p className="text-xs font-medium mb-1.5">{note.title}</p>
            <p className="text-[11px] text-muted leading-relaxed">
              {note.body}
            </p>
            {note.ref && (
              <p className="mt-2 text-[10px] uppercase tracking-wide text-accent">
                → {note.ref}
              </p>
            )}
          </div>
        ))}
      </div>
    ),
  },
  {
    eyebrow: "Daily Walk Tracker",
    title: "Build a rhythm you can see",
    description:
      "Seal your reading and prayer each day on a simple calendar. A quiet streak count keeps you honest about the days you show up.",
    icon: ICONS.flame,
    mock: (
      <div className="rounded-2xl border border-border bg-card p-5">
        <div className="flex items-center justify-between mb-4">
          <span className="font-serif text-base">September</span>
          <span className="text-xs text-accent">14-day streak</span>
        </div>
        <div className="grid grid-cols-7 gap-1.5">
          {CALENDAR_DAYS.map((state, i) => (
            <div
              key={i}
              className={`aspect-square rounded-md flex items-center justify-center text-[10px] ${
                state === 2
                  ? "border-2 border-accent text-accent font-medium"
                  : state === 1
                    ? "bg-accent/15 text-accent"
                    : "border border-border text-muted"
              }`}
            >
              {i + 1}
            </div>
          ))}
        </div>
        <div className="mt-4 pt-4 border-t border-border flex items-center gap-4 text-xs text-muted">
          <span className="flex items-center gap-1.5">
            <Icon path={ICONS.check} className="w-3.5 h-3.5 text-accent" />
            Reading
          </span>
          <span className="flex items-center gap-1.5">
            <Icon path={ICONS.check} className="w-3.5 h-3.5 text-accent" />
            Prayer
          </span>
        </div>
      </div>
    ),
  },
];

const SECONDARY_FEATURES = [
  {
    icon: ICONS.cloud,
    title: "Offline-first & synced",
    description:
      "Everything works with no connection, then syncs quietly across your devices when you're back online.",
  },
];

export default function Home() {
  return (
    <div className="flex flex-col flex-1">
      <SiteHeader />

      <main className="flex-1">
        {/* Hero */}
        <section className="max-w-5xl mx-auto px-6 pt-20 pb-24">
          <div className="max-w-2xl animate-fade-in">
            <p className="text-xs font-medium uppercase tracking-[0.2em] text-muted mb-6">
              A free scripture reading journal
            </p>
            <h1 className="font-serif text-[2.75rem] sm:text-6xl leading-[1.05] tracking-tight text-balance">
              Read scripture with intention. Keep it with you.
            </h1>
            <p className="mt-6 text-lg leading-relaxed text-muted max-w-md">
              Save verses, write notes, and track your daily walk — offline
              by default, synced when you need it.
            </p>
            <div className="mt-9 flex flex-wrap items-center gap-4">
              <a
                href={PLAY_STORE_URL}
                className="inline-flex items-center gap-2 rounded-md bg-foreground text-background px-6 py-3 text-sm font-medium hover:bg-accent transition-colors"
              >
                Get it on Google Play
              </a>
              <span className="inline-flex items-center gap-2 rounded-md border border-border px-6 py-3 text-sm font-medium text-muted">
                iOS — coming soon
              </span>
            </div>
          </div>
        </section>

        {/* Feature showcase */}
        <section id="features" className="border-t border-border">
          <div className="max-w-5xl mx-auto px-6 py-20">
            <h2 className="font-serif text-3xl mb-16">
              Built for a daily rhythm
            </h2>
            <div className="flex flex-col gap-20">
              {SHOWCASE.map((feature, i) => (
                <div
                  key={feature.eyebrow}
                  className="grid gap-10 sm:grid-cols-2 sm:items-center"
                >
                  <div className={i % 2 === 1 ? "sm:order-2" : ""}>
                    <div className="text-accent mb-4">
                      <Icon path={feature.icon} />
                    </div>
                    <p className="text-xs font-medium uppercase tracking-[0.15em] text-muted mb-2">
                      {feature.eyebrow}
                    </p>
                    <h3 className="font-serif text-2xl leading-snug mb-3">
                      {feature.title}
                    </h3>
                    <p className="text-sm leading-relaxed text-muted max-w-sm">
                      {feature.description}
                    </p>
                  </div>
                  <div className={i % 2 === 1 ? "sm:order-1" : ""}>
                    {feature.mock}
                  </div>
                </div>
              ))}
            </div>

            <div className="mt-20 pt-16 border-t border-border">
              {SECONDARY_FEATURES.map((feature) => (
                <div key={feature.title} className="flex gap-4 max-w-md">
                  <div className="text-accent shrink-0">
                    <Icon path={feature.icon} />
                  </div>
                  <div>
                    <h3 className="font-serif text-lg mb-1.5">{feature.title}</h3>
                    <p className="text-sm leading-relaxed text-muted">
                      {feature.description}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Download CTA */}
        <section className="border-t border-border">
          <div className="max-w-5xl mx-auto px-6 py-24 text-center">
            <h2 className="font-serif text-3xl sm:text-4xl mb-4">
              Start your scripture journal today
            </h2>
            <p className="text-muted mb-9">Free to download. No ads.</p>
            <div className="flex flex-wrap items-center justify-center gap-4">
              <a
                href={PLAY_STORE_URL}
                className="inline-flex items-center gap-2 rounded-md bg-foreground text-background px-6 py-3 text-sm font-medium hover:bg-accent transition-colors"
              >
                Get it on Google Play
              </a>
              <span className="inline-flex items-center gap-2 rounded-md border border-border px-6 py-3 text-sm font-medium text-muted">
                iOS — coming soon
              </span>
            </div>
          </div>
        </section>
      </main>

      <SiteFooter />
    </div>
  );
}
