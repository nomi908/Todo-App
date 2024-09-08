package Fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentTextEditBinding
import java.io.File
import java.io.IOException

class TextEditFragment : Fragment() {

    private lateinit var binding: FragmentTextEditBinding
    private val PERMISSION_REQUEST_CODE = 1
    private var currentFile: File? = null
    private var originalFileName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTextEditBinding.inflate(inflater, container, false)

        // Set up the button click listener
        binding.savebtn.setOnClickListener {
            checkAndRequestPermissions()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fileName = arguments?.getString("filePath")
        originalFileName = fileName

        if (fileName != null) {
            binding.texttile.setText(File(fileName).name.replace(".txt", ""))
//            val filePath = getFilePath(fileName)

            // Debug log to verify file path
            Log.d("filegetting", "File path: $fileName")

            currentFile = File(fileName)

            if (currentFile?.exists() == true) {
                try {
                    val myOutput = currentFile?.readText()

                    // Debug log to verify file content
                    Log.d("filegetting", "File content: $myOutput")

                    binding.dspdata.setText(myOutput)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Failed to read file", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "File not found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "No file name provided", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDataAndSwitchFragment() {
        val title = binding.texttile.text.toString()
        val content = binding.dspdata.text.toString()

        if (title.isNotEmpty()) {
            val fileName = "$title.txt"
            val filePath = getFilePath(fileName)
            val file = File(filePath)
            Log.d("saveDataAndSwitchFragment", "file path: "+filePath)

            if (currentFile?.name != fileName) {
                // If the file name is different, delete the old file
                currentFile?.takeIf { it.exists() }?.delete() // Delete old file
                currentFile = file
            }

            writeTextToFile(file, content)
            navigateToHomeFragment()
        } else {
            Toast.makeText(requireContext(), "Title should not be empty!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            saveDataAndSwitchFragment()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveDataAndSwitchFragment()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun writeTextToFile(file: File, data: String) {
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            try {
                file.writeText(data)
                Log.d("TextEditFragment", "Data written successfully to ${file.path}")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("TextEditFragment", "Failed to write data to file", e)
                Toast.makeText(requireContext(), "Failed to save data", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("TextEditFragment", "External storage is not mounted")
            Toast.makeText(requireContext(), "External storage not mounted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToHomeFragment() {
        // Ensure the fragment transaction is properly managed
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragemtslayout, HomeFragment())
            .addToBackStack(null) // Optional: add to back stack if you want to go back to this fragment
            .commit()
    }

    private fun getFilePath(fileName: String): String {
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "TODO Assistant")
        if (!directory.exists()) {
            directory.mkdirs() // Ensure the directory exists
        }
        return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() +"/TODO Assistant/"+fileName).path
    }
}
