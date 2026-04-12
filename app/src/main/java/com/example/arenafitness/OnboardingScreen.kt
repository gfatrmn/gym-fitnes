package com.example.arenafitness

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arenafitness.ui.theme.*
import androidx.compose.foundation.BorderStroke

@Composable
fun OnboardingScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("KINETICNOIR", color = AccentOrange, fontWeight = FontWeight.Bold)
            Text("EDITION // 0.1", color = TextGray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Box container for Logo - Made Circular
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape) // Clip the box to a circle
                .background(Color(0xFF1A1A1A)) // Dark grey background for the box
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Logo Arena Fitness
            Image(
                painter = painterResource(id = R.drawable.arenafitness),
                contentDescription = "Arena Fitness Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "TRAIN HARD.\nLIVE STRONGER.",
            color = TextWhite,
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 46.sp
        )

        Text(
            text = "PEAK PERFORMANCE TRACKING FOR\nTHE UNCOMPROMISING ATHLETE",
            color = TextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Progress Bar
        Row(Modifier.fillMaxWidth(0.5f).padding(bottom = 40.dp)) {
            Box(Modifier.weight(1f).height(4.dp).background(PrimaryRed))
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f).height(4.dp).background(Color.DarkGray))
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f).height(4.dp).background(Color.DarkGray))
        }

        // Main Button (GET STARTED)
        Button(
            onClick = { 
                val intent = Intent(context, RegisterActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
            shape = MaterialTheme.shapes.extraSmall
        ) {
            Text("GET STARTED →", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Outline Button (I ALREADY HAVE AN ACCOUNT)
        OutlinedButton(
            onClick = {
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            border = BorderStroke(1.dp, Color.White),
            shape = MaterialTheme.shapes.extraSmall
        ) {
            Text("I ALREADY HAVE AN ACCOUNT", color = TextWhite)
        }

        Text(
            text = "BY CONTINUING, YOU AGREE TO OUR TERMS OF SERVICE",
            color = TextGray,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}