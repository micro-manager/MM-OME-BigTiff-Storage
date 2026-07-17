# Publishing to Maven Central

This project is published to [Maven Central](https://central.sonatype.com) under the
`org.micro-manager` namespace (verified against the `micro-manager.org` domain).

Coordinates:

```
org.micro-manager:MM-OME-BigTiff-Storage:<version>
```

The release machinery lives in the `release` profile in `pom.xml`. A normal
`mvn package` / `mvn install` does **not** require a GPG key or credentials —
signing and uploading only happen under `-Prelease`.

## One-time setup

### 1. GPG key

All artifacts uploaded to Central must be signed. Create a key (if you don't have one)
and publish the public half to a keyserver so Central can verify the signatures.

```bash
gpg --gen-key                                   # create a key
gpg --list-keys --keyid-format short            # note the KEY_ID
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
```

If GPG can't find a running agent (common on headless/CI setups), pass the passphrase
explicitly at deploy time — see "Troubleshooting" below.

### 2. Central Portal account and namespace

- Sign in at https://central.sonatype.com.
- The `org.micro-manager` namespace must be verified under the account you deploy with.
  (It is already verified against `micro-manager.org`.)

### 3. Central Portal user token

Generate a user token in the Portal: **your avatar → View Account → Generate User Token**.
Add it to your `~/.m2/settings.xml`. The server `id` must be `central` to match
`publishingServerId` in `pom.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>TOKEN_USERNAME</username>
      <password>TOKEN_PASSWORD</password>
    </server>
  </servers>
</settings>
```

## Releasing via GitHub Actions (manual trigger)

The `.github/workflows/publish.yml` workflow deploys to Central from CI. It is
`workflow_dispatch` only — trigger it from the **Actions** tab → **Publish to Maven
Central** → **Run workflow**. A `dry_run` checkbox builds and signs without uploading.

This requires four repository secrets (**Settings → Secrets and variables → Actions**):

| Secret | What it is |
| --- | --- |
| `CENTRAL_USERNAME` | Central Portal user token username |
| `CENTRAL_PASSWORD` | Central Portal user token password |
| `GPG_PRIVATE_KEY` | ASCII-armored private key: `gpg --armor --export-secret-keys <KEY_ID>` (paste the whole block, including the `-----BEGIN/END-----` lines) |
| `GPG_PASSPHRASE` | Passphrase for that GPG key |

The workflow still respects `autoPublish=false`, so a successful run leaves the deployment
in the Portal awaiting the manual **Publish** step (see below). Set the version to a plain
release (no `-SNAPSHOT`) on the branch/commit you run it against.

## Cutting a release (locally)

### 1. Set the release version

Central rejects `-SNAPSHOT` versions. Make sure `pom.xml` has a plain release version
(e.g. `0.1.0`) before deploying. Optionally tag the release in git:

```bash
git tag v0.1.0
git push origin v0.1.0
```

### 2. Dry run (recommended)

Build and sign everything locally without uploading. This catches missing metadata,
javadoc errors, or GPG problems before anything leaves your machine:

```bash
mvn -Prelease clean verify
```

You should see a `.asc` signature generated next to each artifact
(jar, sources jar, javadoc jar, and the pom).

### 3. Deploy

```bash
mvn -Prelease clean deploy
```

This builds the jar, sources jar, javadoc jar, and the shaded `-all` jar; signs them all
with GPG; and uploads a deployment bundle to the Central Portal.

### 4. Publish from the Portal

`autoPublish` is set to `false` in `pom.xml`, so the deployment lands in the Portal as
*validated but not yet released*. Go to
https://central.sonatype.com/publishing/deployments, review the deployment, and click
**Publish** to make it live. Propagation to Maven Central typically takes a few minutes to
a few hours.

To skip the manual step in future releases, set `<autoPublish>true</autoPublish>` in the
`central-publishing-maven-plugin` configuration in `pom.xml`.

### 5. Bump to the next development version

After releasing, set `pom.xml` back to the next `-SNAPSHOT` version for ongoing work
(e.g. `0.2.0-SNAPSHOT`).

## Artifacts published

- `MM-OME-BigTiff-Storage-<version>.jar` — the library
- `MM-OME-BigTiff-Storage-<version>-sources.jar` — sources (required by Central)
- `MM-OME-BigTiff-Storage-<version>-javadoc.jar` — javadoc (required by Central)
- `MM-OME-BigTiff-Storage-<version>.pom` — the POM
- A `.asc` GPG signature for each of the above

## Troubleshooting

**`gpg: signing failed: Inappropriate ioctl for device`** — GPG can't prompt for the
passphrase. Either start the agent, or pass the passphrase on the command line:

```bash
mvn -Prelease clean deploy \
  -Dgpg.passphrase='your-passphrase'
```

**`gpg: no default secret key`** — no signing key is available. Confirm `gpg --list-secret-keys`
shows a key, or select one explicitly with `-Dgpg.keyname=<KEY_ID>`.

**401 / authentication errors on upload** — the `central` server credentials in
`~/.m2/settings.xml` are missing or wrong, or the user token has been rotated. Regenerate
the token in the Portal and update `settings.xml`.

**Namespace / validation errors** — confirm the `groupId` (`org.micro-manager`) matches a
verified namespace under the account whose token you're using.
