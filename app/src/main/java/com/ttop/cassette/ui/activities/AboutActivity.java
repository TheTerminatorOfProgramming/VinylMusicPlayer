package com.ttop.cassette.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.ttop.cassette.BuildConfig;
import com.ttop.cassette.R;
import com.ttop.cassette.databinding.ActivityAboutBinding;
import com.ttop.cassette.dialogs.ChangelogDialog;
import com.ttop.cassette.ui.activities.base.AbsBaseActivity;
import com.ttop.cassette.ui.activities.bugreport.BugReportActivity;
import com.ttop.cassette.ui.activities.intro.AppIntroActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.psdev.licensesdialog.LicensesDialog;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AboutActivity extends AbsBaseActivity {

    Toolbar toolbar;
    TextView appVersion;
    LinearLayout changelog;
    LinearLayout intro;
    LinearLayout licenses;
    LinearLayout writeAnEmail;
    LinearLayout forkOnGitHub;
    LinearLayout visitWebsite;
    LinearLayout reportBugs;

    AppCompatButton adrienPoupaWebsite;
    AppCompatButton kabouzeidWebsite;
    AppCompatButton aidanFollestadGitHub;
    AppCompatButton freepikWebsite;
    AppCompatButton freepikAppIcon;
    AppCompatButton freepikNotifyIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityAboutBinding binding = ActivityAboutBinding.inflate(LayoutInflater.from(this));
        toolbar = binding.toolbar;
        appVersion = binding.content.cardAboutApp.appVersion;
        changelog = binding.content.cardAboutApp.changelog;
        intro = binding.content.cardAboutApp.intro;
        licenses = binding.content.cardAboutApp.licenses;
        forkOnGitHub = binding.content.cardAboutApp.forkOnGithub;

        writeAnEmail = binding.content.cardAuthor.writeAnEmail;
        visitWebsite = binding.content.cardAuthor.visitWebsite;

        reportBugs = binding.content.cardSupportDevelopment.reportBugs;

        kabouzeidWebsite = binding.content.cardSpecialThanks.kabouzeidWebsite;
        aidanFollestadGitHub = binding.content.cardSpecialThanks.aidanFollestadGitHub;
        adrienPoupaWebsite = binding.content.cardSpecialThanks.adrienPoupaWebsite;
        freepikWebsite = binding.content.cardSpecialThanks.freepikWebsite;
        freepikAppIcon = binding.content.cardSpecialThanks.freepikAppIcon;
        freepikNotifyIcon = binding.content.cardSpecialThanks.freepikNotifyIcon;

        setContentView(binding.getRoot());

        setDrawUnderStatusbar();

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        setUpViews();
    }

    private void setUpViews() {
        setUpToolbar();
        setUpAppVersion();
        setUpOnClickListeners();
    }

    private void setUpToolbar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpAppVersion() {
        appVersion.setText(getCurrentVersionName(this));
    }

    private void setUpOnClickListeners() {
        changelog.setOnClickListener(view -> ChangelogDialog.create().show(getSupportFragmentManager(), "CHANGELOG_DIALOG"));

        intro.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), AppIntroActivity.class)));

        licenses.setOnClickListener(view -> showLicenseDialog());

        forkOnGitHub.setOnClickListener(view -> openUrl("https://github.com/TheTerminatorOfProgramming/Cassette"));

        visitWebsite.setOnClickListener(view -> {
            Toast.makeText(getApplicationContext(), "Website Coming Soon", Toast.LENGTH_SHORT).show();
            //openUrl(enter website);
        });

        reportBugs.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), BugReportActivity.class)));

        writeAnEmail.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:ttop.app.dev@gmail.com"));
            intent.putExtra(Intent.EXTRA_EMAIL, "ttop.app.dev@gmail.com");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Cassette");
            startActivity(Intent.createChooser(intent, "E-Mail"));
        });

        aidanFollestadGitHub.setOnClickListener(view -> openUrl("https://github.com/afollestad"));

        adrienPoupaWebsite.setOnClickListener(view -> openUrl("https://adrien.poupa.fr/"));

        kabouzeidWebsite.setOnClickListener(view -> openUrl("https://kabouzeid.com"));

        freepikWebsite.setOnClickListener(v -> openUrl("https://www.freepik.com/"));

        freepikAppIcon.setOnClickListener(v -> openUrl("https://www.flaticon.com/premium-icon/cassette-tape_2842778"));

        freepikNotifyIcon.setOnClickListener(v -> openUrl("https://www.flaticon.com/premium-icon/cassette-tape_2842768"));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static String getCurrentVersionName(@NonNull final Context context) {

        if (BuildConfig.BUILD_TYPE.equals("beta") || BuildConfig.DEBUG){
            Date c = Calendar.getInstance().getTime();
            System.out.println("Current time => " + c);

            SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            String formattedDate = df.format(c);

            try {
                return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName + " " + formattedDate;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return "Unknown";
    }

    private void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void showLicenseDialog() {
        new LicensesDialog.Builder(this)
                .setNotices(R.raw.notices)
                .setTitle(R.string.licenses)
                .setNoticesCssStyle(getString(R.string.license_dialog_style)
                        .replace("{bg-color}", ThemeSingleton.get().darkTheme ? "424242" : "ffffff")
                        .replace("{text-color}", ThemeSingleton.get().darkTheme ? "ffffff" : "000000")
                        .replace("{license-bg-color}", ThemeSingleton.get().darkTheme ? "535353" : "eeeeee")
                )
                .setIncludeOwnLicense(true)
                .build()
                .show();
    }
}