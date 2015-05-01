# Chatter
Location-based Image Sharing Android App

3/15 2015

	* Fixed Parse asynchronously grabbing data and cannot return value after it completes and refresh UI by putting the adapter inside Parse so it makes sure it will update once it finishes grabbing data

3/17 2015

	* Added Camera Intent for user to take picture.
	* Able to upload picture without pressing post button to ensure fast and fluent experience.
	* Able to download post that contains picture and description in background using parse and library Picasso for displaying image from URL in background thread

3/18 2015

	* Reduced the size of the taken picture to optimize both networking and efficient view inflating

3/19 2015

	* Added SplashActivity to determine whether the user should be directed to login screen or main screen based on if the user is signed in yet
	* Added ParseUser so app knows who is using the app
	* Added sign up screen and sign up procedure for registering account using email
	* Added log in screen and log in procedure for logging into the app using email
	* Added log off function at MainActivity to log the current user off the app and return to log in screen

3/22 2015

	* Added Like function for the image when clicking on image, will be moved to clicking a image button to like the photo
	* Attempted to synchronize the number of likes on one post in "Like" and number of likes in "Post". Sometimes it shows a negative number. Solved it by syncing the number of likes in the post with the number of query in "like"
	* Attempted to use double tap mechanism on picture to like to avoid the event mentioned above

4/18 2015

	* Fixed a situation where it will update its location when the function is called or minimum interval time is met
	* App can now know how many people are around the current user within 10 km perimeter and be able to show their pictures in chronologically descending order according to post created time

4/19 2015

	* Tweaked new activity ui
	* Made it into card view

4/20 2015

	* Able to grab post first and then check the location. If the server user location and local user location have difference of less than 10 km, then grab the post and update location afterwards. Otherwise, grab new feed too. If user were to grab new feeds according to new location, user can manually refresh it.
	* By doing the above, it results in better performance and more responsive

4/22 2015

	* Having trouble with setOnScrollListener on the recyclerview to implement infinite scroll

4/23 2015

	* For some reason, my scroll listener needs to be below where I implemented FAB
	* Implemented infinite scroll. It means that when the user has reached the end of stream, the app will automatically grab 10 more posts at the bottom so you can keep scrolling

4/25 2015

	* When I am at Burnaby and my last location is Surrey, I cannot see my own posts but i can see sister's. Solution: add the current user at the end of search no matter what
	* Optimize the code by finally separate out the post function from MainActivity to a Post class

	* Attempting to make ObservableRecyclcerview


4/26 2015

	* Added Observer to observe actions completed by Parse

	* Fixed issue where clickListener on post is not working

