package abbosbek.mobiler.musicplayer3;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // members
    Context context;
    List<Song> songs;
    ExoPlayer player;
    ConstraintLayout playerView;

    public SongAdapter(Context context, List<Song> songs,ExoPlayer player,ConstraintLayout playerView) {
        this.context = context;
        this.songs = songs;
        this.player = player;
        this.playerView = playerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row_item,parent,false);

        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        // current song and view holder
        Song song = songs.get(position);
        SongViewHolder viewHolder = (SongViewHolder) holder;

        // set values to views
        viewHolder.titleHolder.setText(song.getTitle());
        viewHolder.durationHolder.setText(getDuration(song.getDuration()));
        viewHolder.sizeHolder.setText(getSize(song.getSize()));

        // artwork
        Uri artworkUri = song.getArtworkUri();

        if (artworkUri != null){
            viewHolder.artworkHolder.setImageURI(artworkUri);

            if (viewHolder.artworkHolder.getDrawable() == null){
                viewHolder.artworkHolder.setImageResource(R.drawable.default_song);
            }
        }

        // play song on item click
        viewHolder.itemView.setOnClickListener(view -> {
            //start the player service
            context.startService(new Intent(context.getApplicationContext(),PlayerService.class));
            if (!player.isPlaying()){
                player.setMediaItems(getMediaItems(),position,0);
            }else {
                player.pause();
                player.seekTo(position,0);
            }
            player.prepare();
            player.play();
            Toast.makeText(context, song.getTitle(), Toast.LENGTH_SHORT).show();

            //show the player view
            playerView.setVisibility(View.VISIBLE);

            //check if the record audio permission is granted,hence request it
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                // request the record audio perm
                ((MainActivity)context).recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

    }

    private List<MediaItem> getMediaItems() {
        //define a list of media items
        List<MediaItem> mediaItems = new ArrayList<>();
        for (Song song : songs) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(song.getUri())
                    .setMediaMetadata(getMetaData(song))
                    .build();

            // add the media item to media item list
            mediaItems.add(mediaItem);
        }

        return mediaItems;
    }

    private MediaMetadata getMetaData(Song song) {
        return new MediaMetadata.Builder()
                .setTitle(song.getTitle())
                .setArtworkUri(song.getArtworkUri())
                .build();
    }

    //viewHolder
    public static class SongViewHolder extends RecyclerView.ViewHolder{
        // members
        ImageView artworkHolder;
        TextView titleHolder,durationHolder,sizeHolder;
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            artworkHolder = itemView.findViewById(R.id.artworkView);
            titleHolder = itemView.findViewById(R.id.titleView);
            durationHolder = itemView.findViewById(R.id.durationView);
            sizeHolder = itemView.findViewById(R.id.sizeView);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    // filter songs / search results
    public void filterSong(List<Song> filteredList){
        songs = filteredList;
        notifyDataSetChanged();
    }

    private String getDuration(int totalDuration){
        String totalDurationText;

        int hrs = totalDuration/(1000*60*60);
        int min = (totalDuration%(1000*60*60))/(1000*60);
        int secs = ((totalDuration%(1000*60*60))%(100*60*60))%(1000*600)/1000;

        if (hrs < 1){
            totalDurationText = String.format("%02d:%02d",min,secs);
        }else {
            totalDurationText = String.format("%1d:%02d:%02d",hrs,min,secs);
        }
        return totalDurationText;
    }

    private String getSize(long bytes){
        String hrSize;
        double k = bytes/1024.0;
        double m = ((bytes/1024.0)/1024.0);
        double g = (((bytes/1024.0)/1024.0)/1024.0);

        // the format
        DecimalFormat dec = new DecimalFormat("0,00");
        if (g>1){
            hrSize = dec.format(g).concat(" GB");
        }
        else if (m > 1){
            hrSize = dec.format(m).concat(" MB");
        }
        else if (k > 1){
            hrSize = dec.format(k).concat(" KB");
        }
        else {
            hrSize = dec.format(g).concat(" Bytes");
        }
        return hrSize;
    }
}
