package card.loyalty.loyaltycardcustomer.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Arrays;

import card.loyalty.loyaltycardcustomer.R;

/**
 * Created by Sam on 20/05/2017.
 */

public class QrFragment extends Fragment{

    private static final String TAG = "QrFragment";
    private static final int RC_SIGN_IN = 123;

    // Fragment Management Tag
    public static final String FRAGMENT_TAG = "QR";

    // Firebase UID extra for launching MyCards activity
    public static final String EXTRA_FIREBASE_UID = "FIREBASE_UID";

    // QR Code
    private ImageView mQrCodeView;

    // Firebase User ID
    private String mUserID;

    // Firebase Authentication Variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Initialise Firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr, container, false);
        view.setTag(TAG);

        // Get image view for qr code
        mQrCodeView = (ImageView) view.findViewById(R.id.imgview_qr);

        // Get image view for qr code
        mQrCodeView = (ImageView) view.findViewById(R.id.imgview_qr);

        // Firebase UI Authentication
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged: is signed_in:" + user.getUid());
                    onSignedInInitialise(user);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: is signed_out");
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    //.setTheme(R.style.AuthTheme) //set bizName theme for Firebase UI here
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        return view;
    }


    // Once signed in create QR code
    private void onSignedInInitialise(FirebaseUser user) {
        // Get Firebase User ID
        mUserID = user.getUid();

        TextView idView = (TextView) getView().findViewById(R.id.idView);
        idView.setText(mUserID);

        // Set ImageView to QR Code
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(mUserID, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            mQrCodeView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void onSignedOutCleanup() {
        // TODO cleanup
    }

    // On resuming fragment
    @Override
    public void onResume() {
        super.onResume();

        // Add the firebase auth state listener
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    // On pausing fragment
    @Override
    public void onPause() {
        super.onPause();

        // Remove the firebase auth state listener
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    // Handling the Firebase UI Auth result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == getActivity().RESULT_OK) {
                Toast.makeText(getActivity(), "Sign in successful", Toast.LENGTH_SHORT).show();
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Sign in cancelled", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }
}
