package com.it.mytest

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.loader.content.CursorLoader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.it.mytest.databinding.ActivityMainBinding
import com.it.mytest.model.SongsModel
import com.it.mytest.ui.SongsAdapter
import com.it.mytest.util.Utility.getNameFromUri
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), SongsAdapter.OnItemClickListener {


    var duration: String? = null

    companion object {
         var mediaPlayer: MediaPlayer? = null

    }


    private var songsArrayList: ArrayList<SongsModel>? = null

    var timer: ScheduledExecutorService? = null

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    var colum = arrayOf<String>(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )
    var list: ArrayList<Uri>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        list = ArrayList()
        if((ActivityCompat.checkSelfPermission(
                this,colum[0])!= PackageManager.PERMISSION_GRANTED)&&
            (ActivityCompat.checkSelfPermission(
                this,colum[1])!= PackageManager.PERMISSION_GRANTED)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(colum,123);
            }
        }

        initUI()
    }

    private fun initUI() {

        songsArrayList = ArrayList()

        binding.btnPickAudio.setOnClickListener {

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "audio/*"
            resultLauncher.launch(intent)
        }




        binding.seekbar1.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null) {
                    val millis = mediaPlayer!!.currentPosition
                    val total_secs =
                        TimeUnit.SECONDS.convert(millis.toLong(), TimeUnit.MILLISECONDS)
                    val mins = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS)
                    val secs = total_secs - mins * 60
                    binding.tvDuration.setText("$mins:$secs / $duration")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer!!.seekTo(binding.seekbar1!!.progress)
                }
            }
        })

    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val uri = data!!.data

                if (data.getClipData() != null) {
                    val x: Int = data.getClipData()!!.getItemCount()
                    for (i in 0 until x) {
                        list!!.add(data.getClipData()!!.getItemAt(i).getUri())
                    }
                    binding.rvSongs.adapter = SongsAdapter(this@MainActivity, list!!)


                    var layoutManager = LinearLayoutManager(this)
                    binding.rvSongs.layoutManager = layoutManager;


                    binding.rvSongs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            if (layoutManager.findFirstVisibleItemPosition() > 0) {
                                Log.d("SCROLLINGDOWN", "SCROLL")
                                releaseMediaPlayer()
                            } else {
                                Log.d("SCROLLINGUP", "SCROLL")
                            }
                        }
                    })


                } else if (data.getData() != null) {
                    val imgurl: String = data.getData()!!.getPath()!!
                    list!!.add(Uri.parse(imgurl))
                }



                try {
                    val uriString: String = uri.toString()


                  //  val myFile = File(uriString)
                    //    String path = myFile.getAbsolutePath();
                   // val displayName: String? = null
                   // val path2: String = getAudioPath(uri!!)!!
                   // val f = File(path2)

                } catch (e: Exception) {
                    //handle exception
                    Toast.makeText(
                        this,
                        "Unable to process,try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private fun getAudioPath(uri: Uri): String? {
        val data = arrayOf(MediaStore.Audio.Media.DATA)
        val loader = CursorLoader(applicationContext, uri, data, null, null, null)
        val cursor: Cursor = loader.loadInBackground()!!
        val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    fun playAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying()) {
                mediaPlayer!!.pause();
                // binding.btnPickAudio.setText("PLAY");
                timer!!.shutdown();
            } else {
                mediaPlayer!!.start();
                // button2.setText("PAUSE");

                timer = Executors.newScheduledThreadPool(1);
                timer!!.scheduleAtFixedRate({
                    if (mediaPlayer != null) {
                        if (!binding.seekbar1!!.isPressed()) {
                            binding.seekbar1!!.setProgress(mediaPlayer!!.getCurrentPosition());
                        }
                    }
                }, 10, 10, TimeUnit.MILLISECONDS)
            }
        }
    }

    fun createMediaPlayer(uri: Uri?) {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        try {
            mediaPlayer!!.setDataSource(applicationContext, uri!!)
            mediaPlayer!!.prepare()
             binding.tvSongname.setText(getNameFromUri(context = applicationContext,uri))
            // button2.setEnabled(true)
            val millis = mediaPlayer!!.duration
            val total_secs = TimeUnit.SECONDS.convert(millis.toLong(), TimeUnit.MILLISECONDS)
            val mins = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS)
            val secs = total_secs - mins * 60
            duration = "$mins:$secs"
            binding.tvDuration.setText("00:00 / $duration")
            binding.seekbar1!!.max = millis
            binding.seekbar1!!.progress = 0
            mediaPlayer!!.setOnCompletionListener { releaseMediaPlayer() }
        } catch (e: IOException) {
            binding.tvSongname.setText(e.toString())
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }

    fun releaseMediaPlayer() {
        if (timer != null) {
            timer!!.shutdown()
        }
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
        //button2.setEnabled(false)
        // textview2.setText("TITLE")
        binding.tvDuration.setText("00:00 / 00:00")
        binding.seekbar1!!.max = 100
        binding.seekbar1!!.progress = 0
    }

    override fun onItemClick(uri: Uri) {

        createMediaPlayer(uri)
        playAudio()
    }


}