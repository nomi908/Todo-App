package Fragments

import Adapters.NotesAdapter
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentHomeBinding
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val fileList = mutableListOf<File>()
    private lateinit var adapter: NotesAdapter
    private val filteredFileList = mutableListOf<File>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        // Set up Floating Action Button click listener
        binding.floatbtn.setOnClickListener {
            if (adapter.isSelectionMode) {
                adapter.clearSelection()  // Clear selection if in selection mode
                binding.toolbar.visibility = View.GONE
            } else {
                val textEditFragment = TextEditFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragemtslayout, textEditFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        // Initialize Toolbar
        val toolbar = binding.toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple))

        //searchbar code
        val searchEditText: EditText = binding.searchtextbox
        searchEditText.setOnTouchListener { _, _ ->
            searchEditText.isFocusable = true
            searchEditText.isFocusableInTouchMode = true
            searchEditText.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
            true
        }
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFiles(s.toString())
            }



            override fun afterTextChanged(s: Editable?) {}
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.item_context_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                handleDelete()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = binding.rcv
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = NotesAdapter(filteredFileList, { selectedCount ->
            binding.toolbar.visibility = if (selectedCount > 0) View.VISIBLE else View.GONE
        }) { file ->
            if (!adapter.isSelectionMode) {
                // Navigate to EditTextFragment only if not in selection mode
                val textEditFragment = TextEditFragment()
                val bundle = Bundle().apply {
                    putString("filePath", file.absolutePath)
                }
                textEditFragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragemtslayout, textEditFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
        recyclerView.adapter = adapter

        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val todoFolder = File(downloadsDir, "TODO Assistant")

            if (todoFolder.exists() && todoFolder.isDirectory) {
                fileList.clear()
                fileList.addAll(todoFolder.listFiles() ?: arrayOf())
                filteredFileList.addAll(fileList)
                adapter.notifyDataSetChanged()
            }
        }

    }


    private fun handleDelete() {
        adapter.getSelectedItems().forEach { file ->
            file.delete()  // Delete the file
        }
        adapter.clearSelection()
        binding.toolbar.visibility = View.GONE
    }
    private fun filterFiles(query: String) {
        filteredFileList.clear()
        if (query.isEmpty()){
            filteredFileList.addAll(fileList)
        }else{
            filteredFileList.addAll(fileList.filter { it.name.contains(query, ignoreCase = true) })
            val queryLower = query.lowercase()
            fileList.forEach { file ->
                try {
                    file.inputStream().bufferedReader(Charset.defaultCharset()).use { reader ->
                        val content = reader.readText().lowercase()
                        if (content.contains(queryLower)) {
                            filteredFileList.add(file)
                        }
                    }
                } catch (e: IOException) {
                    // Handle file reading error if needed
                }
            }
        }


        adapter.notifyDataSetChanged()
    }
}
