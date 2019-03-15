package de.tu_darmstadt.epool.pfoertnerpanel.helpers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.math.BigInteger;

public class AtheneReader {
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;

    private Activity mContext;

    MediaPlayer mediaPlayer = new MediaPlayer();

    public AtheneReader(Activity context){
        this.mContext = context;

        // get a handle to the local nfc device/adapter
        mAdapter = NfcAdapter.getDefaultAdapter(context);

        pendingIntent = PendingIntent.getActivity(
                context, // this class is the parent of the new activity
                0, // can be set to a custom integer to identify the resulting activity from others
                new Intent(mContext, context.getClass()) // parent and class of the component which shall handle the intent, which is this class
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), // if parent activity is on top of stack, send the intent to its onNewIntent method when triggered
                0 // no special flags
        );

        // we only want to listen for this specific nfc intent
        // See usage of `enableForegroundDispatch` in `onResume`
        IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        intentFiltersArray = new IntentFilter[] {tech, };

        // we only want to discover nfc technology, that supports ISO 14443 (which the athene card does)
        // See usage of `enableForegroundDispatch` in `onResume`
        techListsArray = new String[][] { new String[] { IsoDep.class.getName() } };

        // set up a notification ringtone to be played when an athene card is discovered
        final Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        try {
            mediaPlayer.setDataSource(context, defaultRingtoneUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mediaPlayer.prepare();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause(){
        // when this activity is paused, there is no more need to capture nfc data
        mAdapter.disableForegroundDispatch(mContext);
    }

    public void resume(){
        // when discovering a nfc tag (athene card) dispatch it to this activity, if it is on top
        mAdapter.enableForegroundDispatch(
                mContext,
                pendingIntent, // use this intent for dispatching (see above)
                intentFiltersArray, // filter the possible intents that can be send (see above)
                techListsArray // only discover specific nfc technology (see above)
        );
    }

    public boolean isTechDiscovered(Intent intent){
        return NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction());
    }

    public long extractAtheneId(Intent intent){
        final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG); // the intent will carry the nfc card (tag) as extra data

        BigInteger atheneId = normalizeTagID(tag.getId()); // the tag id can be interpreted in different formats, see normalize method
        return atheneId.longValue();
    }

    public void beep(){
        mediaPlayer.start();
    }

    /**
     * On newer athene cards, the card id is printed above the photo.
     * Using ISO 14443 the card returns this id as a byte array in reverse byte order.
     * This method reverses the byte order of an ID retrieved from a tag and interprets it
     * as an unsigned integer, such that it has the same appearance as printed on the card.
     *
     * @param isoID athene card id as returned by nfc adapter using ISO 14443 / IsoDep
     * @return id interpreted as integer as printed on the newer cards.
     */
    public static BigInteger normalizeTagID(final byte[] isoID) {
        //Byte order on card is reversed, undoing this now
        final byte[] reversedIsoID = ArrayUtils.clone(isoID);
        ArrayUtils.reverse(reversedIsoID);

        //Interpreting as positive integer:
        return new BigInteger(1, reversedIsoID);
    }
}
