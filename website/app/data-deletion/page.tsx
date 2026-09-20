import type { Metadata } from "next";
import Link from "next/link";
import LegalPage from "@/components/LegalPage";

export const metadata: Metadata = {
  title: "Request Data Deletion — FaithFlow",
  description: "How to delete your FaithFlow account and associated data.",
};

export default function DataDeletion() {
  return (
    <LegalPage title="Request Data Deletion" updated="May 2026">
      <p>
        At FaithFlow, we respect your privacy and give you full control over
        your data. If you wish to delete your account and all associated
        personal data from our systems (including your saved verses, themes,
        notes, and Google authentication ties), you can do so using either of
        the methods below.
      </p>

      <div className="rounded-lg border-l-4 border-accent bg-card p-5 flex flex-col gap-3">
        <h2 className="!mt-0">Method 1: In-App Deletion (Recommended)</h2>
        <p>
          The fastest way to permanently delete your data is directly from
          within the FaithFlow app.
        </p>
        <ol>
          <li>
            Open the <strong>FaithFlow</strong> app on your device.
          </li>
          <li>Make sure you are signed in to your account.</li>
          <li>
            Tap the <strong>Menu (⋮)</strong> icon in the top-right corner of
            the main screen.
          </li>
          <li>
            Tap <strong>Delete Account</strong> and confirm your choice.
          </li>
        </ol>
        <p className="italic text-muted">
          Note: This action is immediate and irreversible. All your synced
          data on Supabase and your authentication record in Firebase will be
          permanently erased.
        </p>
      </div>

      <div className="rounded-lg border-l-4 border-accent bg-card p-5 flex flex-col gap-3">
        <h2 className="!mt-0">Method 2: Email Request</h2>
        <p>
          If you no longer have access to the app or your device, you can
          request manual data deletion by contacting us directly.
        </p>
        <p>Please send an email to our support team with the following details:</p>
        <ul>
          <li>
            <strong>To:</strong> rhine.pereira@gmail.com
          </li>
          <li>
            <strong>Subject:</strong> Account Data Deletion Request
          </li>
          <li>
            <strong>Body:</strong> Please include the Email Address
            associated with your FaithFlow (Google Auth) account so we can
            locate and securely delete your data.
          </li>
        </ul>
        <p>
          We will process your request within 7 business days and confirm
          once your data has been completely removed from our servers.
        </p>
      </div>

      <h2>What Happens When You Delete Your Account?</h2>
      <p>
        Once your deletion request is processed (either automatically in-app
        or manually via email), the following data is permanently purged:
      </p>
      <ul>
        <li>Your user authentication profile (Google Sign-in/Firebase token).</li>
        <li>All your saved Bible verses and themes.</li>
        <li>All your personal notes and customized daily records.</li>
        <li>Any diagnostic or usage data linked to your identity.</li>
      </ul>

      <p>
        <Link href="/privacy">&larr; Return to Privacy Policy</Link>
      </p>
    </LegalPage>
  );
}
