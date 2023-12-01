// Get all the video elements in the web page
var videos = document.querySelectorAll('video');

// Loop through each video element
for (var i = 0; i < videos.length; i++) {
  // Get the current video element
  var video = videos[i];

  // Add a listener for the play event
  video.addEventListener('play', function() {
    // Check if the video element supports the webkitEnterFullscreen method
    if (video.webkitEnterFullscreen) {
      // Enter the full-screen mode
      video.webkitEnterFullscreen();
    }
  });
}

window.AddVideoControls = function() {

  // Get the video element and its parent container
  var video = document.querySelector('video');
  var container = video.parentElement;

  // Create a div element to hold the buttons
  var controls = document.createElement('div');
  controls.style.position = 'absolute';
  controls.style.bottom = '10px';
  controls.style.left = '10px';
  controls.style.zIndex = '9999';

  // Create the rewind button
  var rewind = document.createElement('button');
  rewind.textContent = '<<';
  rewind.onclick = function() {
    // Seek 10 seconds backward
    video.currentTime -= 10;
  };

  // Create the play/pause button
  var playpause = document.createElement('button');
  playpause.textContent = video.paused ? 'Play' : 'Pause';
  playpause.onclick = function() {
    // Toggle the playback state
    if (video.paused) {
      video.play();
      playpause.textContent = 'Pause';
    } else {
      video.pause();
      playpause.textContent = 'Play';
    }
  };

  // Create the forward button
  var forward = document.createElement('button');
  forward.textContent = '>>';
  forward.onclick = function() {
    // Seek 10 seconds forward
    video.currentTime += 10;
  };

  // Create the fullscreen button
  var fullscreen = document.createElement('button');
  fullscreen.textContent = 'Fullscreen';
  fullscreen.onclick = function() {
    // Request the fullscreen mode
    if (video.requestFullscreen) {
      video.requestFullscreen();
    } else if (video.webkitRequestFullscreen) {
      video.webkitRequestFullscreen();
    } else if (video.mozRequestFullScreen) {
      video.mozRequestFullScreen();
    } else if (video.msRequestFullscreen) {
      video.msRequestFullscreen();
    }
  };

  // Append the buttons to the controls div
  controls.appendChild(rewind);
  controls.appendChild(playpause);
  controls.appendChild(forward);
  controls.appendChild(fullscreen);

  // Append the controls div to the container
  container.appendChild(controls);
};
