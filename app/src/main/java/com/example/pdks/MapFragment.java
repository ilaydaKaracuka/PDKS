package com.example.pdks;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.Overlay;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private MapView mapView;
    private TilesOverlay labelOverlay;
    private MyLocationNewOverlay myLocationOverlay;
    private FusedLocationProviderClient fusedLocationClient;
    private String entityId;
    private String type;

    private ActivityResultLauncher<Intent> cameraLauncher;

    private final List<GeoPoint> blockPoints = new ArrayList<>();
    private final List<String> blockNames = new ArrayList<>();
    private final List<Polygon> blockCircles = new ArrayList<>();
    private final List<Overlay> blockLabels = new ArrayList<>();

    private MaterialButton btnSaveLocation;

    private final XYTileSource esriSatellite = new XYTileSource(
            "EsriWorldImagery", 0, 19, 256, ".jpg",
            new String[]{"https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"}) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            return getBaseUrl() +
                    MapTileIndex.getZoom(pMapTileIndex) + "/" +
                    MapTileIndex.getY(pMapTileIndex) + "/" +
                    MapTileIndex.getX(pMapTileIndex) + ".jpg";
        }
    };

    private final XYTileSource cartoLabels = new XYTileSource(
            "CartoLight", 0, 19, 256, ".png",
            new String[]{"https://cartodb-basemaps-a.global.ssl.fastly.net/light_all/"}) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            return getBaseUrl() +
                    MapTileIndex.getZoom(pMapTileIndex) + "/" +
                    MapTileIndex.getX(pMapTileIndex) + "/" +
                    MapTileIndex.getY(pMapTileIndex) + ".png";
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == CameraActivity.RESULT_OK) {
                        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            goToUserLocation(() -> saveRecordToDatabase(ImageStorage.getBitmap()));
                        } else {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                        }
                    } else {
                        Toast.makeText(requireContext(), "İnsan doğrulaması başarısız!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = root.findViewById(R.id.mapview);
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(19.0);
        mapView.getController().setCenter(new GeoPoint(41.0382184, 28.8866255));

        labelOverlay = new TilesOverlay(
                new org.osmdroid.tileprovider.MapTileProviderBasic(requireContext(), cartoLabels),
                requireContext()
        );
        labelOverlay.setLoadingBackgroundColor(0x00000000);

        Bundle args = getArguments();
        if (args != null) {
            entityId = args.getString("ENTITY_ID");
            type = args.getString("type", "GIRIS");
        } else {
            type = "GIRIS";
        }

        Log.d("MapFragment", "Received entityId: " + entityId);

        Button btnStandard = root.findViewById(R.id.btn_standard);
        Button btnSatellite = root.findViewById(R.id.btn_satellite);
        Button btnHybrid = root.findViewById(R.id.btn_hybrid);
        btnSaveLocation = root.findViewById(R.id.btn_save_location);
        ImageView locationIcon = root.findViewById(R.id.location_icon2);
        ImageView btnZoomIn = root.findViewById(R.id.btn_zoom_in);
        ImageView btnZoomOut = root.findViewById(R.id.btn_zoom_out);

        btnSaveLocation.setText(type.equals("GIRIS") ? "Giriş Konumu Kaydet" : "Çıkış Konumu Kaydet");
        btnSaveLocation.setEnabled(false);

        btnSaveLocation.setOnClickListener(v -> {
            if (btnSaveLocation.isEnabled()) {
                if (isVpnActive()) {
                    Toast.makeText(requireContext(), "VPN açıkken kayıt yapılamaz!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (isAutoTimeEnabled()) {
                    Intent cameraIntent = new Intent(requireContext(), CameraActivity.class);
                    cameraLauncher.launch(cameraIntent);
                } else {
                    showAutoTimeWarning();
                }
            }
        });


        View.OnClickListener mapTypeClickListener = v -> {
            if (v == btnStandard) {
                mapView.setTileSource(TileSourceFactory.MAPNIK);
                mapView.getOverlays().remove(labelOverlay);
            } else if (v == btnSatellite) {
                mapView.setTileSource(esriSatellite);
                mapView.getOverlays().remove(labelOverlay);
            } else if (v == btnHybrid) {
                mapView.setTileSource(esriSatellite);
                if (!mapView.getOverlays().contains(labelOverlay)) mapView.getOverlays().add(labelOverlay);
            }
            mapView.invalidate();
        };
        btnStandard.setOnClickListener(mapTypeClickListener);
        btnSatellite.setOnClickListener(mapTypeClickListener);
        btnHybrid.setOnClickListener(mapTypeClickListener);

        locationIcon.setOnClickListener(v -> goToUserLocation(null));
        btnZoomIn.setOnClickListener(v -> mapView.getController().zoomIn());
        btnZoomOut.setOnClickListener(v -> mapView.getController().zoomOut());

        addBlocks();

        return root;
    }

    private void addBlocks() {
        blockPoints.add(new GeoPoint(41.0382184, 28.8866255));
        blockPoints.add(new GeoPoint(41.0384875, 28.8862446));
        blockPoints.add(new GeoPoint(41.0404837, 28.8850628));

        blockNames.add("Esenler-A Blok");
        blockNames.add("Esenler-B Blok");
        blockNames.add("Esenler-C Blok");

        for (int i = 0; i < blockPoints.size(); i++) {
            final GeoPoint point = blockPoints.get(i);
            final String labelText = blockNames.get(i);

            Polygon circle = new Polygon(mapView);
            circle.setPoints(Polygon.pointsAsCircle(point, 50.0));
            circle.setFillColor(Color.parseColor("#80BC3E51"));
            circle.setStrokeColor(Color.parseColor("#8E0000"));
            circle.setStrokeWidth(6);
            blockCircles.add(circle);
            mapView.getOverlays().add(circle);

            circle.setOnClickListener((polygon, mapView1, eventPos) -> {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    if (myLocationOverlay == null) initMyLocationOverlay();

                    new android.os.Handler().postDelayed(() -> {
                        GeoPoint userLocation = myLocationOverlay.getMyLocation();
                        if (userLocation != null) {
                            double distance = userLocation.distanceToAsDouble(point);
                            Toast.makeText(getContext(), "Uzaklığınız: " + String.format("%.2f", distance) + " m", Toast.LENGTH_LONG).show();

                            for (Polygon c : blockCircles) {
                                c.setFillColor(Color.parseColor("#80BC3E51"));
                                c.setStrokeColor(Color.parseColor("#8E0000"));
                            }

                            circle.setFillColor(Color.parseColor("#8038E83C"));
                            circle.setStrokeColor(Color.parseColor("#388E3C"));

                            if (distance <= 50.0) {
                                btnSaveLocation.setEnabled(true);
                                btnSaveLocation.setBackgroundTintList(getResources().getColorStateList(R.color.mavi_buton2));
                            } else {
                                btnSaveLocation.setEnabled(false);
                                btnSaveLocation.setBackgroundTintList(getResources().getColorStateList(R.color.acik_mavi));
                            }

                            mapView.invalidate();
                        } else {
                            Toast.makeText(getContext(), "Konum Bulunamadı", Toast.LENGTH_SHORT).show();
                        }
                    }, 1000);
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                }
                return true;
            });

            Overlay textOverlay = new Overlay() {
                @Override
                public void draw(Canvas c, MapView osmv, boolean shadow) {
                    if (!shadow) {
                        Point screenPoint = new Point();
                        osmv.getProjection().toPixels(point, screenPoint);
                        Paint paint = new Paint();
                        paint.setColor(Color.BLACK);
                        paint.setTextSize(40);
                        paint.setTextAlign(Paint.Align.CENTER);
                        c.drawText(labelText, screenPoint.x, screenPoint.y + 15, paint);
                    }
                }
            };
            blockLabels.add(textOverlay);
            mapView.getOverlays().add(textOverlay);
        }
        mapView.invalidate();
    }

    private void goToUserLocation(Runnable afterLocationFound) {
        if (myLocationOverlay == null) initMyLocationOverlay();

        new android.os.Handler().postDelayed(() -> {
            if (myLocationOverlay.getMyLocation() != null) {
                GeoPoint userLocation = myLocationOverlay.getMyLocation();
                mapView.getController().animateTo(userLocation);
                mapView.getController().setZoom(19.0);
                if (afterLocationFound != null) afterLocationFound.run();
            } else {
                Toast.makeText(getContext(), "Konum Bulunamadı", Toast.LENGTH_SHORT).show();
            }
        }, 1000);
    }

    private void initMyLocationOverlay() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.runOnFirstFix(() -> requireActivity().runOnUiThread(() -> {
            if (myLocationOverlay.getMyLocation() != null) {
                mapView.getController().animateTo(myLocationOverlay.getMyLocation());
                mapView.getController().setZoom(17.0);
            }
        }));
        mapView.getOverlays().add(myLocationOverlay);
    }

    private void saveRecordToDatabase(Bitmap capturedBitmap) {
        if (isVpnActive()) {
            Toast.makeText(getContext(), "VPN açıkken kayıt yapılamaz!", Toast.LENGTH_LONG).show();
            return;
        }
        if (entityId == null || entityId.isEmpty()) {
            Toast.makeText(getContext(), "entityId boş! Kayıt yapılamıyor.", Toast.LENGTH_SHORT).show();
            Log.e("MapFragment", "entityId null veya boş.");
            return;
        }

        if (myLocationOverlay == null || myLocationOverlay.getMyLocation() == null) {
            Toast.makeText(getContext(), "Konum alınamadı! Kayıt yapılamıyor.", Toast.LENGTH_SHORT).show();
            Log.e("MapFragment", "Konum null.");
            return;
        }

        GeoPoint userLocation = myLocationOverlay.getMyLocation();
        String selectedBlock = null;
        double minDistance = Double.MAX_VALUE;

        Polygon clickedCircle = null;
        for (Polygon c : blockCircles) {
            if (c.getFillColor() == Color.parseColor("#8038E83C")) {
                clickedCircle = c;
                break;
            }
        }

        if (clickedCircle != null) {
            int index = blockCircles.indexOf(clickedCircle);
            double distance = userLocation.distanceToAsDouble(blockPoints.get(index));
            if (distance <= 50.0) {
                selectedBlock = blockNames.get(index);
            }
        }

        if (selectedBlock == null) {
            for (int i = 0; i < blockPoints.size(); i++) {
                double distance = userLocation.distanceToAsDouble(blockPoints.get(i));
                if (distance <= 50.0 && distance < minDistance) {
                    minDistance = distance;
                    selectedBlock = blockNames.get(i);
                }
            }
        }

        if (selectedBlock == null) {
            Toast.makeText(getContext(), "Hiçbir bloğun içinde değilsiniz!", Toast.LENGTH_SHORT).show();
            Log.e("MapFragment", "Hiçbir blokta değil.");
            return;
        }

        String tarih = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String saat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String tip = type.equals("GIRIS") ? "Giriş Kaydı" : "Çıkış Kaydı";

        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.KayitEntry.COLUMN_NAME_ENTITY_ID, entityId);
        values.put(DatabaseContract.KayitEntry.COLUMN_NAME_BLOK_ADI, selectedBlock);
        values.put(DatabaseContract.KayitEntry.COLUMN_NAME_TARIH, tarih);
        values.put(DatabaseContract.KayitEntry.COLUMN_NAME_SAAT, saat);
        values.put(DatabaseContract.KayitEntry.COLUMN_NAME_TIP, tip);

        if (capturedBitmap != null) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(capturedBitmap, 200, 200, true);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 80, stream);
            values.put(DatabaseContract.KayitEntry.COLUMN_NAME_PHOTO, stream.toByteArray());
        }

        long newRowId = db.insert(DatabaseContract.KayitEntry.TABLE_NAME, null, values);
        db.close();

        if (newRowId != -1) {
            Toast.makeText(getContext(), "Kayıt başarıyla eklendi!", Toast.LENGTH_SHORT).show();
            Log.d("MapFragment", "Kayıt eklendi, id=" + newRowId);
        } else {
            Toast.makeText(getContext(), "Kayıt eklenemedi!", Toast.LENGTH_SHORT).show();
            Log.e("MapFragment", "Kayıt eklenemedi!");
        }
    }

    private boolean isVpnActive() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            for (Network network : cm.getAllNetworks()) {
                NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goToUserLocation(null);
            } else {
                Toast.makeText(getContext(), "Konum izni gerekli!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isAutoTimeEnabled() {
        try {
            return Settings.Global.getInt(requireContext().getContentResolver(), Settings.Global.AUTO_TIME) == 1;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAutoTimeWarning() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Saat Ayarı Hatası")
                .setMessage("Lütfen telefonunuzun tarih ve saat ayarlarını 'Otomatik' moda alın. Aksi halde kayıt atamazsınız.")
                .setPositiveButton("Tamam", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
