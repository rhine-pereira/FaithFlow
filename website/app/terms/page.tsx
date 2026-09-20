import type { Metadata } from "next";
import Link from "next/link";
import LegalPage from "@/components/LegalPage";

export const metadata: Metadata = {
  title: "Terms & Conditions — FaithFlow",
  description: "The terms that govern your use of the FaithFlow app.",
};

export default function TermsAndConditions() {
  return (
    <LegalPage title="Terms and Conditions" updated="May 2026">
      <p>
        Please read these terms and conditions carefully before using the
        FaithFlow mobile application (the &ldquo;Service&rdquo;).
      </p>

      <h2>1. Acceptance of Terms</h2>
      <p>
        By accessing or using the Service, You agree to be bound by these
        Terms and Conditions. If You disagree with any part of these terms,
        then You may not access the Service. Your use of the Service is also
        governed by our <Link href="/privacy">Privacy Policy</Link>.
      </p>

      <h2>2. Accounts and Authentication</h2>
      <ul>
        <li>
          When You create an account with Us using Google Sign-In, You must
          guarantee that the information provided is accurate and complete.
        </li>
        <li>
          You are responsible for safely maintaining the confidentiality of
          your account credentials.
        </li>
        <li>
          We reserve the right to terminate or suspend your account
          immediately, without prior notice or liability, for any reason,
          including without limitation if you breach the Terms.
        </li>
      </ul>

      <h2>3. User Content</h2>
      <p>
        Our Service allows You to post, link, store, share and otherwise make
        available certain information, text (such as personal notes and
        Bible verses), or other material (&ldquo;Content&rdquo;).
      </p>
      <ul>
        <li>You retain ownership of the Content you submit.</li>
        <li>
          You are solely responsible for the Content that you post on or
          through the Service.
        </li>
        <li>
          We reserve the right, but not the obligation, to monitor and edit
          all Content provided by users.
        </li>
      </ul>

      <h2>4. Acceptable Use</h2>
      <p>You agree not to use the Service:</p>
      <ul>
        <li>
          In any way that violates any applicable national or international
          law or regulation.
        </li>
        <li>
          For the purpose of exploiting, harming, or attempting to exploit or
          harm minors in any way.
        </li>
        <li>
          To transmit, or procure the sending of, any advertising or
          promotional material, including any &ldquo;junk mail&rdquo;,
          &ldquo;chain letter,&rdquo; &ldquo;spam,&rdquo; or any other similar
          solicitation.
        </li>
      </ul>

      <h2>5. Intellectual Property</h2>
      <p>
        The Service and its original content (excluding Content provided by
        users), features, and functionality are and will remain the
        exclusive property of FaithFlow and its licensors. The Service is
        protected by copyright, trademark, and other laws of both the
        Country and foreign countries.
      </p>

      <h2>6. Limitation of Liability</h2>
      <p>
        In no event shall FaithFlow, nor its directors, employees, partners,
        agents, suppliers, or affiliates, be liable for any indirect,
        incidental, special, consequential or punitive damages, including
        without limitation, loss of profits, data, use, goodwill, or other
        intangible losses, resulting from (i) your access to or use of or
        inability to access or use the Service; (ii) any conduct or content
        of any third party on the Service; (iii) any content obtained from
        the Service; and (iv) unauthorized access, use or alteration of your
        transmissions or content.
      </p>

      <h2>7. &ldquo;AS IS&rdquo; and &ldquo;AS AVAILABLE&rdquo; Disclaimer</h2>
      <p>
        The Service is provided to You &ldquo;AS IS&rdquo; and &ldquo;AS
        AVAILABLE&rdquo; and with all faults and defects without warranty of
        any kind. We do not warrant that the Service will function
        uninterrupted, secure or available at any particular time or
        location.
      </p>

      <h2>8. Changes to These Terms</h2>
      <p>
        We reserve the right, at Our sole discretion, to modify or replace
        these Terms at any time. By continuing to access or use Our Service
        after those revisions become effective, You agree to be bound by the
        revised terms.
      </p>

      <h2>9. Contact Us</h2>
      <p>
        If you have any questions about these Terms, please contact us at{" "}
        <strong>rhine.pereira@gmail.com</strong>.
      </p>
    </LegalPage>
  );
}
